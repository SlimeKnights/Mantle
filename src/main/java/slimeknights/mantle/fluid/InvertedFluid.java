package slimeknights.mantle.fluid;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.Map;

/** Fluid where up is down and down is up */
public abstract class InvertedFluid extends BaseFlowingFluid {
  protected InvertedFluid(Properties properties) {
    super(properties);
  }

  @Override
  public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState fluid) {
    double xHeight = 0.0D;
    double zHeight = 0.0D;
    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      mutable.setWithOffset(pos, direction);
      FluidState sideFluid = level.getFluidState(mutable);
      if (this.affectsFlow(sideFluid)) {
        float sideHeight = sideFluid.getOwnHeight();
        float deltaHeight = 0.0F;
        if (sideHeight == 0.0F) {
          if (!level.getBlockState(mutable).blocksMotion()) {
            BlockPos above = mutable.above();
            FluidState aboveFluid = level.getFluidState(above);
            if (this.affectsFlow(aboveFluid)) {
              sideHeight = aboveFluid.getOwnHeight();
              if (sideHeight > 0.0F) {
                deltaHeight = fluid.getOwnHeight() - sideHeight + 0.8888889F;
              }
            }
          }
        } else if (sideHeight > 0.0F) {
          deltaHeight = fluid.getOwnHeight() - sideHeight;
        }

        if (deltaHeight != 0.0F) {
          xHeight += direction.getStepX() * deltaHeight;
          zHeight += direction.getStepZ() * deltaHeight;
        }
      }
    }

    Vec3 vector = new Vec3(xHeight, 0.0D, zHeight);
    if (fluid.getValue(FALLING)) {
      for (Direction direction : Direction.Plane.HORIZONTAL) {
        mutable.setWithOffset(pos, direction);
        if (this.isSolidFace(level, mutable, direction) || this.isSolidFace(level, mutable.below(), direction)) {
          vector = vector.normalize().add(0.0D, 6.0D, 0.0D);
          break;
        }
      }
    }

    return vector.normalize();
  }

  @Override
  protected boolean isSolidFace(BlockGetter level, BlockPos neighbor, Direction side) {
    BlockState block = level.getBlockState(neighbor);
    FluidState fluid = level.getFluidState(neighbor);
    return !fluid.getType().isSame(this) && (side == Direction.DOWN || !(block.getBlock() instanceof IceBlock) && block.isFaceSturdy(level, neighbor, side));
  }

  @Override
  protected void spread(Level level, BlockPos pos, FluidState fluid) {
    // recreation that swaps downs for ups
    if (!fluid.isEmpty()) {
      BlockState block = level.getBlockState(pos);
      BlockPos above = pos.above();
      BlockState aboveBlock = level.getBlockState(above);
      FluidState aboveFluid = this.getNewLiquid(level, above, aboveBlock);
      if (this.canSpreadTo(level, pos, block, Direction.UP, above, aboveBlock, level.getFluidState(above), aboveFluid.getType())) {
        this.spreadTo(level, above, aboveBlock, Direction.UP, aboveFluid);
        if (this.sourceNeighborCount(level, pos) >= 3) {
          this.spreadToSides(level, pos, fluid, block);
        }
      } else if (fluid.isSource() || !this.isWaterHole(level, aboveFluid.getType(), pos, block, above, aboveBlock)) {
        this.spreadToSides(level, pos, fluid, block);
      }
    }
  }

  @Override
  protected FluidState getNewLiquid(Level level, BlockPos pos, BlockState block) {
    int maxSide = 0;
    int sourceSides = 0;

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      BlockPos side = pos.relative(direction);
      BlockState sideBlock = level.getBlockState(side);
      FluidState sideFluid = sideBlock.getFluidState();
      if (sideFluid.getType().isSame(this) && this.canPassThroughWall(direction, level, pos, block, side, sideBlock)) {
        if (sideFluid.isSource() && EventHooks.canCreateFluidSource(level, side, sideBlock, sideFluid.canConvertToSource(level, side))) {
          sourceSides++;
        }
        maxSide = Math.max(maxSide, sideFluid.getAmount());
      }
    }

    if (sourceSides >= 2) {
      BlockState aboveBlock = level.getBlockState(pos.above());
      FluidState aboveFluid = aboveBlock.getFluidState();
      if (aboveBlock.isSolid() || this.isSourceBlockOfThisType(aboveFluid)) {
        return this.getSource(false);
      }
    }

    BlockPos below = pos.below();
    BlockState belowBlock = level.getBlockState(below);
    FluidState belowFluid = belowBlock.getFluidState();
    if (!belowFluid.isEmpty() && belowFluid.getType().isSame(this) && this.canPassThroughWall(Direction.DOWN, level, pos, block, below, belowBlock)) {
      return this.getFlowing(8, true);
    }
    int newHeight = maxSide - this.getDropOff(level);
    return newHeight <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(newHeight, false);
  }


  @Override
  protected int getSlopeDistance(LevelReader level, BlockPos spreadPos, int distance, Direction direction, BlockState spreadBlock, BlockPos sourcePos, Short2ObjectMap<Pair<BlockState, FluidState>> stateCache, Short2BooleanMap waterHoleCache) {
    int minSlope = 1000;

    for (Direction horizontal : Direction.Plane.HORIZONTAL) {
      if (horizontal != direction) {
        BlockPos side = spreadPos.relative(horizontal);
        short key = getCacheKey(sourcePos, side);
        Pair<BlockState, FluidState> state = stateCache.computeIfAbsent(key, (p_284932_) -> {
          BlockState sideBlock = level.getBlockState(side);
          return Pair.of(sideBlock, sideBlock.getFluidState());
        });
        BlockState sideBlock = state.getFirst();
        FluidState sideFluid = state.getSecond();
        if (this.canPassThrough(level, this.getFlowing(), spreadPos, spreadBlock, horizontal, side, sideBlock, sideFluid)) {
          boolean isWaterHole = waterHoleCache.computeIfAbsent(key, k -> {
            BlockPos above = side.above();
            BlockState aboveState = level.getBlockState(above);
            return this.isWaterHole(level, this.getFlowing(), side, sideBlock, above, aboveState);
          });
          if (isWaterHole) {
            return distance;
          }
          if (distance < this.getSlopeFindDistance(level)) {
            int slopeDistance = this.getSlopeDistance(level, side, distance + 1, horizontal.getOpposite(), sideBlock, sourcePos, stateCache, waterHoleCache);
            if (slopeDistance < minSlope) {
              minSlope = slopeDistance;
            }
          }
        }
      }
    }

    return minSlope;
  }

  @Override
  protected boolean isWaterHole(BlockGetter level, Fluid fluid, BlockPos pos, BlockState block, BlockPos spreadPos, BlockState spreadBlock) {
    // recreation swapping downs for ups
    return this.canPassThroughWall(Direction.UP, level, pos, block, spreadPos, spreadBlock)
      && (spreadBlock.getFluidState().getType().isSame(this) || this.canHoldFluid(level, spreadPos, spreadBlock, fluid));
  }

  @Override
  protected Map<Direction, FluidState> getSpread(Level level, BlockPos pos, BlockState block) {
    int minDistance = 1000;
    Map<Direction, FluidState> spread = Maps.newEnumMap(Direction.class);
    Short2ObjectMap<Pair<BlockState, FluidState>> stateCache = new Short2ObjectOpenHashMap<>();
    Short2BooleanMap waterHoleCache = new Short2BooleanOpenHashMap();

    for(Direction direction : Direction.Plane.HORIZONTAL) {
      BlockPos side = pos.relative(direction);
      short key = getCacheKey(pos, side);
      Pair<BlockState, FluidState> pair = stateCache.computeIfAbsent(key, k -> {
        BlockState sideBlock = level.getBlockState(side);
        return Pair.of(sideBlock, sideBlock.getFluidState());
      });
      BlockState sideBlock = pair.getFirst();
      FluidState sideFluid = pair.getSecond();
      FluidState newFluid = this.getNewLiquid(level, side, sideBlock);
      if (this.canPassThrough(level, newFluid.getType(), pos, block, direction, side, sideBlock, sideFluid)) {
        BlockPos above = side.above();
        boolean isWaterHole = waterHoleCache.computeIfAbsent(key, (p_255612_) -> {
          BlockState aboveBlock = level.getBlockState(above);
          return this.isWaterHole(level, this.getFlowing(), side, sideBlock, above, aboveBlock);
        });
        int distance = isWaterHole ? 0 : this.getSlopeDistance(level, side, 1, direction.getOpposite(), sideBlock, pos, stateCache, waterHoleCache);
        if (distance < minDistance) {
          spread.clear();
        }
        if (distance <= minDistance) {
          spread.put(direction, newFluid);
          minDistance = distance;
        }
      }
    }

    return spread;
  }

  public static class Flowing extends InvertedFluid {
    public Flowing(Properties properties) {
      super(properties);
      this.registerDefaultState(this.getStateDefinition().any().setValue(LEVEL, 7));
    }

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
      super.createFluidStateDefinition(builder);
      builder.add(LEVEL);
    }

    @Override
    public int getAmount(FluidState state) {
      return state.getValue(LEVEL);
    }

    @Override
    public boolean isSource(FluidState state) {
      return false;
    }
  }

  public static class Source extends InvertedFluid {
    public Source(Properties properties) {
      super(properties);
    }

    @Override
    public int getAmount(FluidState state) {
      return 8;
    }

    @Override
    public boolean isSource(FluidState state) {
      return true;
    }
  }
}

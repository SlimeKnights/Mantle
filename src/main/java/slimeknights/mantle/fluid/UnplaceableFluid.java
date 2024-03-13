package slimeknights.mantle.fluid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.registration.FluidBuilder;

import java.util.function.Supplier;

/** Fluid with a bucket form, but no block form (hence no flowing) */
@AllArgsConstructor
public class UnplaceableFluid extends Fluid {
  /** Forge fluid attributes builder */
  private final Supplier<? extends FluidType> type;
  /** Bucket form of the liquid, use a supplier to air if no bucket form */
  @Nullable
  private final Supplier<? extends Item> bucket;
  @Getter
  private final float explosionResistance;
  private final int tickRate;

  public UnplaceableFluid(FluidBuilder<?> builder) {
    type = builder.getType();
    bucket = builder.getBucket();
    explosionResistance = builder.getExplosionResistance();
    tickRate = builder.getTickRate();
  }

  @SuppressWarnings("unused")  // API
  public UnplaceableFluid(Supplier<? extends FluidType> type, @Nullable Supplier<? extends Item> bucket) {
    this(type, bucket, 100, 5);
  }


  @Override
  public FluidType getFluidType() {
    return type.get();
  }

  @Override
  public Item getBucket() {
    if (bucket == null) {
      return Items.AIR;
    }
    return bucket.get();
  }

  @Override
  protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction side) {
    return false;
  }

  @Override
  public int getTickDelay(LevelReader world) {
    return tickRate;
  }

  @Override
  protected BlockState createLegacyBlock(FluidState state) {
    return Blocks.AIR.defaultBlockState();
  }


  /* Required methods */

  @Override
  protected Vec3 getFlow(BlockGetter world, BlockPos pos, FluidState state) {
    return Vec3.ZERO;
  }

  @Override
  public boolean isSource(FluidState state) {
    return true;
  }

  @Override
  public float getOwnHeight(FluidState state) {
    return 1;
  }

  @Override
  public float getHeight(FluidState state, BlockGetter world, BlockPos pos) {
    return 1;
  }

  @Override
  public int getAmount(FluidState state) {
    return 0;
  }

  @Override
  public VoxelShape getShape(FluidState state, BlockGetter world, BlockPos pos) {
    return Shapes.block();
  }
}

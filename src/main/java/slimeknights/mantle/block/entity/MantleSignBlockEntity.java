package slimeknights.mantle.block.entity;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.registration.MantleRegistrations;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Sign block entity to make it easier for signs to be registered, as the vanilla block entity has a closed set of blocks */
public class MantleSignBlockEntity extends SignBlockEntity {
  /** Sign blocks to use for the blcok entity valid blocks */
  private static final List<Supplier<? extends Block>> SIGN_BLOCKS = new ArrayList<>();
  public MantleSignBlockEntity(BlockPos pos, BlockState state) {
    super(pos, state);
  }

  @Override
  public BlockEntityType<?> getType() {
    return MantleRegistrations.SIGN;
  }

  /**
   * Registers a sign block to be injected into the tile entity, should be called before common setup
   * @param sign  Sign block supplier
   */
  public static void registerSignBlock(Supplier<? extends Block> sign) {
    synchronized (SIGN_BLOCKS) {
      SIGN_BLOCKS.add(sign);
    }
  }

  /** Builds the list of sign blocks for TE registration */
  public static void buildSignBlocks(ImmutableSet.Builder<Block> builder) {
    SIGN_BLOCKS.forEach(block -> builder.add(block.get()));
  }
}

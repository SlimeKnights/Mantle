package slimeknights.mantle.data.predicate.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.block.BlockPredicate;

/**
 * Predicate matching entities with the given block at their feet.
 * @param block  Block to check for
 * @param offset Amount to offset. If 0, feet position. -1 is the block below the feet.
 */
public record BlockAtEntityPredicate(IJsonPredicate<BlockState> block, int offset) implements LivingEntityPredicate {
  public static final RecordLoadable<BlockAtEntityPredicate> LOADER = RecordLoadable.create(
    BlockPredicate.LOADER.directField("block_type", BlockAtEntityPredicate::block),
    IntLoadable.ANY_BYTE.defaultField("offset", 0, false, BlockAtEntityPredicate::offset),
    BlockAtEntityPredicate::new);

  @Override
  public RecordLoadable<BlockAtEntityPredicate> getLoader() {
    return LOADER;
  }

  @Override
  public boolean matches(LivingEntity entity) {
    BlockPos pos = entity.blockPosition();
    if (offset != 0) {
      pos = pos.relative(Axis.Y, offset);
    }
    return block.matches(entity.level().getBlockState(pos));
  }
}

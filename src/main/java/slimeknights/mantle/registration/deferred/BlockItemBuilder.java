package slimeknights.mantle.registration.deferred;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.item.tooltip.FixedTooltipBlockItem;
import slimeknights.mantle.item.tooltip.OptionalTooltipBlockItem;

import java.util.function.Function;

/**
 * Builder for setting block items. Used in {@link BlockDeferredRegister}
 */
@SuppressWarnings("unused")  // API
public interface BlockItemBuilder<B> {
  /** Default item properties instance for the fallback */
  Item.Properties DEFAULT_ITEM_PROPERTIES = new Item.Properties();
  /** Default block item using the default properties. */
  Function<Block,BlockItem> BLOCK_ITEM = block -> new BlockItem(block, DEFAULT_ITEM_PROPERTIES);
  /** Default block item using the default properties. */
  Function<Block,BlockItem> OPTIONAL_TOOLTIP_ITEM = block -> new OptionalTooltipBlockItem(block, DEFAULT_ITEM_PROPERTIES);
  /** Default block item using the default properties. */
  Function<Block,BlockItem> FIXED_TOOLTIP_ITEM = block -> FixedTooltipBlockItem.fromBlock(block, DEFAULT_ITEM_PROPERTIES);

  /** Sets the block item for this builder. On {@link BlockDeferredRegister} sets the default block item for all future calls. */
  B blockItem(Function<? super Block,? extends BlockItem> item);

  /** Sets the block item form to {@link BlockItem}. */
  default B defaultBlockItem() {
    return blockItem(BLOCK_ITEM);
  }

  /** Sets the block item form to {@link OptionalTooltipBlockItem}. */
  default B optionalBlockTooltip() {
    return blockItem(OPTIONAL_TOOLTIP_ITEM);
  }

  /** Sets the block item form to {@link FixedTooltipBlockItem}. */
  default B fixedBlockTooltip() {
    return blockItem(FIXED_TOOLTIP_ITEM);
  }
}

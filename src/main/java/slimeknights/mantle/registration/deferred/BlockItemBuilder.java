package slimeknights.mantle.registration.deferred;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.item.tooltip.BlockTooltipItem;
import slimeknights.mantle.item.tooltip.FixedTooltipBlockItem;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Builder for setting block items. Used in {@link BlockDeferredRegister}
 */
@SuppressWarnings("unused")  // API
public interface BlockItemBuilder<B> {
  /** Default item properties instance for the fallback */
  Item.Properties DEFAULT_ITEM_PROPERTIES = new Item.Properties();
  /** Default block item using the default properties. */
  BiFunction<ResourceLocation,Block,BlockItem> BLOCK_ITEM = (id, b) -> new BlockItem(b, DEFAULT_ITEM_PROPERTIES);
  /** Default block item using the default properties. */
  BiFunction<ResourceLocation,Block,BlockItem> OPTIONAL_TOOLTIP_ITEM = (id, b) -> new BlockTooltipItem(b, DEFAULT_ITEM_PROPERTIES);
  /** Default block item using the default properties. */
  BiFunction<ResourceLocation,Block,BlockItem> FIXED_TOOLTIP_ITEM = (id, b) -> FixedTooltipBlockItem.fromId(id, b, DEFAULT_ITEM_PROPERTIES);

  /** Sets the block item for this builder. On {@link BlockDeferredRegister} sets the default block item for all future calls. */
  B blockItem(BiFunction<ResourceLocation, ? super Block, ? extends BlockItem> item);

  /** Sets the block item for this builder */
  default B blockItem(Function<? super Block, ? extends BlockItem> item) {
    return blockItem((id, block) -> item.apply(block));
  }

  /** Sets the block item form to {@link BlockTooltipItem}. */
  default B defaultBlockItem() {
    return blockItem(BLOCK_ITEM);
  }


  /** Sets the block item form to {@link BlockTooltipItem}. */
  default B optionalBlockTooltip() {
    return blockItem(OPTIONAL_TOOLTIP_ITEM);
  }

  /** Sets the block item form to {@link FixedTooltipBlockItem}. */
  default B fixedBlockTooltip() {
    return blockItem(FIXED_TOOLTIP_ITEM);
  }
}

package slimeknights.mantle.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.util.TranslationHelper;

import java.util.List;

public class BlockTooltipItem extends BlockItem {
  public BlockTooltipItem(Block blockIn, Item.Properties builder) {
    super(blockIn, builder);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
    super.appendHoverText(stack, context, tooltip, flagIn);
    TranslationHelper.addOptionalTooltip(stack, tooltip);
  }
}

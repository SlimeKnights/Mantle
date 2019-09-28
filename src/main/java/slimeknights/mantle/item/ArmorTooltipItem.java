package slimeknights.mantle.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorTooltipItem extends ArmorItem {

  public ArmorTooltipItem(IArmorMaterial armorMaterial, EquipmentSlotType equipmentSlot, Item.Properties builder) {
    super(armorMaterial, equipmentSlot, builder);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    TooltipItem.addOptionalTooltip(stack, tooltip);
    super.addInformation(stack, worldIn, tooltip, flagIn);
  }
}

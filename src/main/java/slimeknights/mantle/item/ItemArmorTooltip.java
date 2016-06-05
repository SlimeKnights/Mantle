package slimeknights.mantle.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemArmorTooltip extends ItemArmor {

  public ItemArmorTooltip(ArmorMaterial armorMaterial, int renderIndex, EntityEquipmentSlot equipmentSlot) {
    super(armorMaterial, renderIndex, equipmentSlot);
  }

  @Override
  public void addInformation(ItemStack p_addInformation_1_, EntityPlayer p_addInformation_2_, List<String> p_addInformation_3_, boolean p_addInformation_4_) {
    ItemTooltip.addOptionalTooltip(p_addInformation_1_, p_addInformation_3_);
    super.addInformation(p_addInformation_1_, p_addInformation_2_, p_addInformation_3_, p_addInformation_4_);
  }
}

package slimeknights.mantle.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.TranslationHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorTooltipItem extends ArmorItem {

  public ArmorTooltipItem(ArmorMaterial armorMaterial, EquipmentSlot equipmentSlot, Settings builder) {
    super(armorMaterial, equipmentSlot, builder);
  }

  @Override
  @Environment(EnvType.CLIENT)
  public void appendTooltip(ItemStack stack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendTooltip(stack, worldIn, tooltip, flagIn);
  }
}

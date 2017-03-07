package slimeknights.mantle.client;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class CreativeTab extends CreativeTabs {

  private ItemStack icon;

  // a vanilla icon in case the other one isn't present
  public CreativeTab(String label, ItemStack backupIcon) {
    super(label);

    this.icon = backupIcon;
  }

  public void setDisplayIcon(ItemStack displayIcon) {
    if(!displayIcon.isEmpty() && displayIcon.getItem() != null) {
      this.icon = displayIcon;
    }
  }

  @Nonnull
  @SideOnly(Side.CLIENT)
  @Override
  public ItemStack getIconItemStack() {
    return icon;
  }

  @Nonnull
  @SideOnly(Side.CLIENT)
  @Override
  public ItemStack getTabIconItem() {
    return icon;
  }
}

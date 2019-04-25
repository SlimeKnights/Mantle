package slimeknights.mantle.client;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class CreativeTab extends ItemGroup {

  private ItemStack icon;

  // a vanilla icon in case the other one isn't present
  public CreativeTab(String label, ItemStack backupIcon) {
    super(label);

    this.icon = backupIcon;
  }

  public void setDisplayIcon(ItemStack displayIcon) {
    if(!displayIcon.isEmpty()) {
      this.icon = displayIcon;
    }
  }

  @Nonnull
  @OnlyIn(Dist.CLIENT)
  @Override
  public ItemStack getIcon() {
    return icon;
  }

  @Nonnull
  @OnlyIn(Dist.CLIENT)
  @Override
  public ItemStack createIcon() {
    return icon;
  }
}

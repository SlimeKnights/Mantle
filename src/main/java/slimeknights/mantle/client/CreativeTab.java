package slimeknights.mantle.client;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CreativeTab extends ItemGroup {

  private ItemStack icon;

  // a vanilla icon in case the other one isn't present
  public CreativeTab(String label, ItemStack backupIcon) {
    super(label);

    this.icon = backupIcon;
  }

  public void setDisplayIcon(ItemStack displayIcon) {
    if (!displayIcon.isEmpty()) {
      this.icon = displayIcon;
    }
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public ItemStack getIcon() {
    return this.icon;
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public ItemStack createIcon() {
    return this.icon;
  }
}

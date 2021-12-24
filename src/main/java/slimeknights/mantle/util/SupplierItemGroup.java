package slimeknights.mantle.util;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

/**
 * Item group that sets its item based on an item supplier
 */
public class SupplierItemGroup extends CreativeModeTab {
  private final Supplier<ItemStack> supplier;

  /**
   * Creates a new item group
   * @param modId     Tab owner mod ID
   * @param name      Tab name
   * @param supplier  Item stack supplier
   */
  public SupplierItemGroup(String modId, String name, Supplier<ItemStack> supplier) {
    super(String.format("%s.%s", modId, name));
    this.setRecipeFolderName(String.format("%s/%s", modId, name));
    this.supplier = supplier;
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public ItemStack makeIcon() {
    return supplier.get();
  }
}

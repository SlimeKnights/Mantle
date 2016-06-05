package slimeknights.mantle.item;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import slimeknights.mantle.util.LocUtils;

/**
 * Item representing a dynamic amount of different items represented by meta.
 * Only returns valid metadatas. The validity is determined by a bitmask.
 * Current maximum is 64 metas - that should suffice for most applications.
 */
public class ItemMetaDynamic extends ItemTooltip {

  private static int MAX = (2 << 16) - 1;

  protected boolean[] availabilityMask; // boolean[] actually is much more performant for our case than BitSet
  protected TIntObjectHashMap<String> names;

  public ItemMetaDynamic() {
    availabilityMask = new boolean[1];
    names = new TIntObjectHashMap<String>();

    this.setHasSubtypes(true);
  }

  /**
   * Add a valid metadata.
   *
   * @param meta The metadata value
   * @param name The name used for registering the itemmodel as well as the unlocalized name of the meta. The unlocalized name will be lowercased and stripped of whitespaces.
   * @return An itemstack representing the Item-Meta combination.
   */
  public ItemStack addMeta(int meta, String name) {
    if(meta > MAX) {
      throw new IllegalArgumentException(String
                                             .format("Metadata for %s too high, highest supported value is %d. Meta was %d", name, MAX, meta));
    }
    else if(isValid(meta)) {
      throw new IllegalArgumentException(String
                                             .format("Metadata for %s is already taken. Meta %d is %s", name, meta, names
                                                 .get(meta)));
    }

    while(meta >= availabilityMask.length) {
      availabilityMask = Arrays.copyOf(availabilityMask, availabilityMask.length * 2);
    }
    setValid(meta);
    names.put(meta, name);
    return new ItemStack(this, 1, meta);
  }

  @Nonnull
  @Override
  public String getUnlocalizedName(ItemStack stack) {
    int meta = stack.getMetadata(); // should call getMetadata below
    if(isValid(meta)) {
      return super.getUnlocalizedName(stack) + "." + LocUtils.makeLocString(names.get(meta));
    }
    else {
      return super.getUnlocalizedName(stack);
    }
  }

  @Override
  public void getSubItems(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    for(int i = 0; i <= availabilityMask.length; i++) {
      if(isValid(i)) {
        subItems.add(new ItemStack(itemIn, 1, i));
      }
    }
  }

  @Override
  public int getMetadata(int damage) {
    int meta = super.getMetadata(damage);
    return isValid(meta) ? meta : 0;
  }

  protected void setValid(int meta) {
    availabilityMask[meta] = true;
  }

  protected boolean isValid(int meta) {
    if(meta > MAX || meta >= availabilityMask.length) {
      return false;
    }
    return availabilityMask[meta];
  }

  @SideOnly(Side.CLIENT)
  public void registerItemModels() {
    registerItemModels(this);
  }

  @SideOnly(Side.CLIENT)
  protected void registerItemModels(final Item item) {
    final String resourceId = Loader.instance().activeModContainer().getModId().toLowerCase();
    names.forEachEntry(new TIntObjectProcedure<String>() {
      @Override
      public boolean execute(int meta, String name) {
        // tell the game which model to use for this item-meta combination
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), name));
        return true;
      }
    });
  }
}

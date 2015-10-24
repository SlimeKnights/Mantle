package slimeknights.mantle.item;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

import slimeknights.mantle.util.LocUtils;

/**
 * Item representing a dynamic amount of different items represented by meta.
 * Only returns valid metadatas. The validity is determined by a bitmask.
 * Current maximum is 64 metas - that should suffice for most applications.
 */
public class ItemMetaDynamic extends Item {

  private static int MAX = Long.SIZE;

  protected int maxMeta;
  protected long availabilityMask;
  protected TIntObjectHashMap<String> names;

  public ItemMetaDynamic() {
    maxMeta = -1;
    names = new TIntObjectHashMap<String>();

    this.setHasSubtypes(true);
  }

  /**
   * Add a valid metadata.
   * @param meta  The metadata value
   * @param name  The name used for registering the itemmodel as well as the unlocalized name of the meta. The unlocalized name will be lowercased and stripped of whitespaces.
   * @return An itemstack representing the Item-Meta combination.
   */
  public ItemStack addMeta(int meta, String name) {
    if(meta > MAX) {
      throw new IllegalArgumentException(String
                                             .format("Metadata too high, highest supported value is %d. Meta was %d", MAX, meta));
    }
    if(meta > maxMeta) {
      maxMeta = meta;
      setValid(meta);
    }
    names.put(meta, name);
    return new ItemStack(this, 1, meta);
  }

  @Override
  public String getUnlocalizedName(ItemStack stack) {
    int meta = stack.getMetadata(); // should call getMetadata below
    if(isValid(meta))
      return super.getUnlocalizedName(stack) + "." + LocUtils.makeLocString(names.get(meta));
    else
      return super.getUnlocalizedName(stack);
  }

  @Override
  public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
    for(int i = 0; i <= maxMeta; i++) {
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
    availabilityMask |= 1 << meta;
  }

  protected boolean isValid(int meta) {
    if(meta > MAX) {
      return false;
    }
    return ((availabilityMask >> meta) & 1) == 1;
  }

  @SideOnly(Side.CLIENT)
  public void registerItemModels(final String prefix) {
    final String resourceId = Loader.instance().activeModContainer().getModId().toLowerCase();
    final Item item = this;
    names.forEachEntry(new TIntObjectProcedure<String>() {
      @Override
      public boolean execute(int meta, String name) {
        name = String.format("%s:%s%s", resourceId, prefix, name);
        ModelBakery.addVariantName(item, name);
        // tell the game which model to use for this item-meta combination
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name, "inventory"));
        return true;
      }
    });
  }

  @Override
  public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
    if(StatCollector.canTranslate(this.getUnlocalizedName(stack) + ".tooltip")) {
      tooltip.add(EnumChatFormatting.GRAY.toString() +
                  LocUtils.translateRecursive(this.getUnlocalizedName(stack) + ".tooltip"));
    }
    else if(StatCollector.canTranslate(super.getUnlocalizedName(stack) + ".tooltip")) {
      tooltip.add(EnumChatFormatting.GRAY.toString() + LocUtils.translateRecursive(super.getUnlocalizedName(stack) + ".tooltip"));
    }
    super.addInformation(stack, playerIn, tooltip, advanced);
  }
}

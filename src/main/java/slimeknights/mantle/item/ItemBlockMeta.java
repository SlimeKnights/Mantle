package slimeknights.mantle.item;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemColored;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Locale;

public class ItemBlockMeta extends ItemColored {

  protected IProperty mappingProperty;

  public ItemBlockMeta(Block block) {
    super(block, true);
  }

  @Override
  public String getUnlocalizedName(ItemStack stack) {
    if(mappingProperty == null) return super.getUnlocalizedName(stack);

    IBlockState state = block.getStateFromMeta(stack.getMetadata());
    String name = state.getValue(mappingProperty).toString().toLowerCase(Locale.US);
    return super.getUnlocalizedName(stack) + "." + name;
  }

  public static void setMappingProperty(Block block, IProperty property) {
    ((ItemBlockMeta)Item.getItemFromBlock(block)).mappingProperty = property;
  }

  @SideOnly(Side.CLIENT)
  public void registerItemModels() {
    final Item item = this;
    final ResourceLocation loc = (ResourceLocation) GameData.getBlockRegistry().getNameForObject(block);


    for(Comparable o : (Collection<Comparable>)mappingProperty.getAllowedValues()) {
      int meta = block.getMetaFromState(block.getDefaultState().withProperty(mappingProperty, o));
      String name = mappingProperty.getName(o);

      ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(loc, mappingProperty.getName() + "=" + name));
    }
  }
}

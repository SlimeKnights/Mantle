package slimeknights.mantle.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EnumBlock<E extends Enum<E> & EnumBlock.IEnumMeta> extends Block {
  public final PropertyEnum prop;
  private final E[] values;

  private static PropertyEnum tmp;

  public EnumBlock(Material material, PropertyEnum prop, Class<E> clazz) {
    super(preInit(material, prop));
    this.prop = prop;
    values = clazz.getEnumConstants();
  }

  private static Material preInit(Material material, PropertyEnum property) {
    tmp=property;
    return material;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
    for(E type : values) {
      list.add(new ItemStack(this, 1, type.getMeta()));
    }
  }

  @Override
  protected BlockState createBlockState() {
    if(prop == null) {
      return new BlockState(this, tmp);
    }
    return new BlockState(this, prop);
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return this.getDefaultState().withProperty(prop, fromMeta(meta));
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return ((E) state.getValue(prop)).getMeta();
  }

  @Override
  public int damageDropped(IBlockState state) {
    return getMetaFromState(state);
  }

  protected E fromMeta(int meta) {
    if(meta < 0 || meta >= values.length) {
      meta = 0;
    }

    return values[meta];
  }

  public interface IEnumMeta {
    int getMeta();
  }
}


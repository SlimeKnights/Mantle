package slimeknights.mantle.block;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;

public abstract class EnumBlockSlab<E extends Enum<E> & EnumBlock.IEnumMeta & IStringSerializable> extends BlockSlab {

  public final PropertyEnum<E> prop;
  private final E[] values;

  private static PropertyEnum<?> tmp;

  public EnumBlockSlab(Material material, PropertyEnum<E> prop, Class<E> clazz) {
    super(preInit(material, prop));
    this.prop = prop;
    values = clazz.getEnumConstants();
    this.setDefaultState(this.blockState.getBaseState().withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM));
    this.useNeighborBrightness = true;
  }

  private static Material preInit(Material material, PropertyEnum<?> property) {
    tmp = property;
    return material;
  }

  @Override
  public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
    for(E type : values) {
      list.add(new ItemStack(this, 1, type.getMeta()));
    }
  }

  @Nonnull
  @Override
  protected BlockStateContainer createBlockState() {
    if(prop == null) {
      return new BlockStateContainer(this, HALF, tmp);
    }
    return new BlockStateContainer(this, HALF, prop);
  }


  /**
   * Convert the given metadata into a BlockState for this Block
   */
  @Nonnull
  @Override
  public IBlockState getStateFromMeta(int meta) {
    return this.getDefaultState()
               .withProperty(prop, fromMeta(meta & 7))
               .withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
  }

  /**
   * Convert the BlockState into the correct metadata value
   */
  @Override
  public int getMetaFromState(IBlockState state) {
    int i = 0;
    i = i | state.getValue(prop).getMeta();

    if(state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
      i |= 8;
    }

    return i;
  }

  @Override
  public int damageDropped(IBlockState state) {
    return state.getValue(prop).getMeta();
  }

  protected E fromMeta(int meta) {
    if(meta < 0 || meta >= values.length) {
      meta = 0;
    }

    return values[meta];
  }

  @Nonnull
  @Override
  public IProperty<E> getVariantProperty() {
    return prop;
  }

  @Nonnull
  @Override
  public Comparable<?> getTypeForItem(@Nonnull ItemStack stack) {
    return fromMeta(stack.getItemDamage() & 7);
  }

  @Nonnull
  @Override
  public String getUnlocalizedName(int meta) {
    return super.getUnlocalizedName() + "." + fromMeta(meta & 7).getName();
  }

  /**
   * Gets the full variant of the slab, as double slabs are not used here
   *
   * @param state Input slab state, in most cases state.getValue() with a switch is all you need
   * @return An IBlockState of the full block
   */
  public abstract IBlockState getFullBlock(IBlockState state);

  // all our slabs are single
  @Override
  public boolean isDouble() {
    return false;
  }

}

package slimeknights.mantle.item;

import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemEdible extends ItemFood {

  // we use this so we don't have to copy all the logic
  private ItemMetaDynamic dynamic;

  protected TIntIntHashMap foodLevels;
  protected TIntFloatHashMap saturations;
  protected TIntObjectHashMap<PotionEffect[]> potionEffects;

  public ItemEdible() {
    super(0, 0, false);
    this.setHasSubtypes(true);

    dynamic = new ItemMetaDynamic();
    foodLevels = new TIntIntHashMap();
    saturations = new TIntFloatHashMap();
    potionEffects = new TIntObjectHashMap<PotionEffect[]>();
  }

  /**
   * Add a new food type!
   * @param meta        Metadata to use, has to be free
   * @param food        How much food it restores on eating
   * @param saturation  Saturation multiplier on the  food
   * @param name        Unlocalized postfix
   * @param effects     PotionEffects that will be applied on eating. The PotionEffect passed will be directly applied
   * @return Itemstack containing the registered item
   */
  public ItemStack addFood(int meta, int food, float saturation, String name, PotionEffect... effects) {
    dynamic.addMeta(meta, name);
    foodLevels.put(meta, food);
    saturations.put(meta, saturation);
    potionEffects.put(meta, effects);

    return new ItemStack(this, 1, meta);
  }

  @Override
  public float getSaturationModifier(ItemStack stack) {
    return saturations.get(stack.getMetadata()); // should call getMetadata below
    // default value if meta not present is 0
  }

  @Override
  public int getHealAmount(ItemStack stack) {
    return foodLevels.get(stack.getMetadata()); // should call getMetadata below
    // default value if meta not present is 0
  }

  @Override
  protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
    if (!worldIn.isRemote)
    {
      for(PotionEffect potion : potionEffects.get(stack.getMetadata())) {
        player.addPotionEffect(new PotionEffect(potion.getPotionID(), potion.getDuration(), potion.getAmplifier(), potion.getIsAmbient(), potion.getIsShowParticles()));
      }
    }
  }

  /* ItemMetaDynamic Functionality */
  @Override
  public Item setUnlocalizedName(String unlocalizedName) {
    dynamic.setUnlocalizedName(unlocalizedName);
    return super.setUnlocalizedName(unlocalizedName);
  }

  @Override
  public String getUnlocalizedName(ItemStack stack) {
    return dynamic.getUnlocalizedName(stack);
  }

  @Override
  public int getMetadata(ItemStack stack) {
    return dynamic.getMetadata(stack);
  }

  @Override
  public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
    dynamic.addInformation(stack, playerIn, tooltip, advanced);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
    dynamic.getSubItems(itemIn, tab, subItems);
  }

  @SideOnly(Side.CLIENT)
  public void registerItemModels(final String prefix) {
    dynamic.registerItemModels(prefix, this);
  }
}

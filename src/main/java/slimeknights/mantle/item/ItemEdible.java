package slimeknights.mantle.item;

import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.BitSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemEdible extends ItemFood {

  // we use this so we don't have to copy all the logic
  private ItemMetaDynamic dynamic;

  protected TIntIntHashMap foodLevels;
  protected TIntFloatHashMap saturations;
  protected TIntObjectHashMap<PotionEffect[]> potionEffects;
  protected BitSet alwaysEdible;

  public boolean displayEffectsTooltip; // set to false to not display effects of food in tooltip

  public ItemEdible() {
    super(0, 0, false);
    this.setHasSubtypes(true);

    dynamic = new ItemMetaDynamic();
    foodLevels = new TIntIntHashMap();
    saturations = new TIntFloatHashMap();
    potionEffects = new TIntObjectHashMap<PotionEffect[]>();
    alwaysEdible = new BitSet();

    displayEffectsTooltip = true;
  }

  public ItemStack addFood(int meta, int food, float saturation, String name, PotionEffect... effects) {
    return addFood(meta, food, saturation, name, effects.length > 0, effects);
  }

  /**
   * Add a new food type!
   *
   * @param meta         Metadata to use, has to be free
   * @param food         How much food it restores on eating
   * @param saturation   Saturation multiplier on the  food
   * @param name         Unlocalized postfix
   * @param alwaysEdible Item is always edible even when hunger is full
   * @param effects      PotionEffects that will be applied on eating. The PotionEffect passed will be directly applied
   * @return Itemstack   containing the registered item
   */
  public ItemStack addFood(int meta, int food, float saturation, String name, boolean alwaysEdible, PotionEffect... effects) {
    dynamic.addMeta(meta, name);
    foodLevels.put(meta, food);
    saturations.put(meta, saturation);
    potionEffects.put(meta, effects);
    this.alwaysEdible.set(meta, alwaysEdible);

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
  protected void onFoodEaten(ItemStack stack, World worldIn, @Nonnull EntityPlayer player) {
    if(!worldIn.isRemote) {
      for(PotionEffect potion : potionEffects.get(stack.getMetadata())) {
        player.addPotionEffect(new PotionEffect(potion.getPotion(), potion.getDuration(), potion.getAmplifier(), potion
            .getIsAmbient(), potion.doesShowParticles()));
      }
    }
  }

  /* ItemMetaDynamic Functionality */
  @Nonnull
  @Override
  public Item setUnlocalizedName(@Nonnull String unlocalizedName) {
    dynamic.setUnlocalizedName(unlocalizedName);
    return super.setUnlocalizedName(unlocalizedName);
  }

  @Nonnull
  @Override
  public String getUnlocalizedName(ItemStack stack) {
    return dynamic.getUnlocalizedName(stack);
  }

  @Override
  public int getMetadata(ItemStack stack) {
    return dynamic.getMetadata(stack);
  }

  @Override
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    dynamic.addInformation(stack, worldIn, tooltip, flagIn);

    // effect info
    if(displayEffectsTooltip) {
      for(PotionEffect potionEffect : potionEffects.get(stack.getMetadata())) {
        tooltip.add(I18n.translateToLocal(potionEffect.getEffectName()).trim());
      }
    }
  }

  @Override
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if(this.isInCreativeTab(tab)) {
      for(int i = 0; i <= dynamic.availabilityMask.length; i++) {
        if(dynamic.isValid(i)) {
          subItems.add(new ItemStack(this, 1, i));
        }
      }
    }
  }

  @SideOnly(Side.CLIENT)
  public void registerItemModels() {
    dynamic.registerItemModels(this);
  }

  @Nonnull
  @Override
  public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, @Nonnull EnumHand hand)
  {
    @Nonnull ItemStack itemStackIn = playerIn.getHeldItem(hand);
    int meta = itemStackIn.getMetadata();
    if(dynamic.isValid(meta) && playerIn.canEat(this.alwaysEdible.get(itemStackIn.getMetadata())))
    {
      playerIn.setActiveHand(hand);
      return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }
    else
    {
      return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStackIn);
    }
  }

}

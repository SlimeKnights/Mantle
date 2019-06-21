package slimeknights.mantle.util;

import java.util.Random;

import javax.annotation.Nonnull;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity.PriceInfo;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class TradeGeneric implements VillagerTrades.ITrade {
  private ItemStack input, input2, output;
  private PriceInfo inputPrice, input2Price, outputPrice;

  /**
   * Creates a new trade of two input stacks for an output stack
   *
   * @param input        First input itemstack
   * @param inputPrice   Random stack size range for first input
   * @param input2       Second input itemstack
   * @param input2Price  Random stack size range for second input
   * @param output       Output itemstack
   * @param outputPrice  Random stack size range for output
   */
  public TradeGeneric(ItemStack input, PriceInfo inputPrice, ItemStack input2, PriceInfo input2Price,
      ItemStack output, PriceInfo outputPrice) {

    this.input = input;
    this.inputPrice = inputPrice;

    this.input2 = input2;
    this.input2Price = input2Price;

    this.output = output;
    this.outputPrice = outputPrice;
  }

  /**
   * Creates a new trade of an input stack for an output stack
   *
   * @param input        Input itemstack
   * @param inputPrice   Random stack size range for input
   * @param output       Output itemstack
   * @param outputPrice  Random stack size range for output
   */
  public TradeGeneric(ItemStack input, PriceInfo inputPrice, ItemStack output, PriceInfo outputPrice) {
    this(input, inputPrice, ItemStack.EMPTY, null, output, outputPrice);
  }

  /**
   * Creates a new trade with one input for emeralds
   *
   * @param input        Input itemstack
   * @param inputPrice   Random stack size range for input
   * @param outputPrice  Random range for output emerald
   */
  public TradeGeneric(ItemStack input, PriceInfo inputPrice, PriceInfo outputPrice) {
    this(input, inputPrice, ItemStack.EMPTY, null, ItemStack.EMPTY, outputPrice);
  }

  /**
   * Creates a new trade of emeralds for an output stack
   *
   * @param inputPrice   Random stack size range for input
   * @param output       Output itemstack
   * @param outputPrice  Random range for output emerald
   */
  public TradeGeneric(PriceInfo inputPrice, ItemStack output, PriceInfo outputPrice) {
    this(ItemStack.EMPTY, inputPrice, ItemStack.EMPTY, null, output, outputPrice);
  }

  /**
   * Getter for the input stack
   * @param random  random number access
   * @return  A new instance of the input stack, or a stack containing an emerald if empty
   */
  @Nonnull
  protected ItemStack getInput(Random random) {
    if(input.isEmpty()) {
      return new ItemStack(Items.EMERALD);
    }

    return input.copy();
  }

  /**
   * Getter for the second input stack, returns a new instance
   * @param random  random number access
   * @return  A new instance of the second input stack, or nothing if empty
   */
  protected ItemStack getInput2(Random random) {
    // no copy if empty
    if(input2.isEmpty()) {
      return ItemStack.EMPTY;
    }
    return input2.copy();
  }

  /**
   * Getter for the output stack
   * @param random  random number access
   * @return  A new instance of the output stack, or a stack containing an emerald if null
   */
  @Nonnull
  protected ItemStack getOutput(Random random) {
    if(output.isEmpty()) {
      return new ItemStack(Items.EMERALD);
    }

    return output.copy();
  }

  @Override
  public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
    ItemStack input = getInput(random);
    ItemStack output = getOutput(random);

    // set random counts
    int size = 1;
    if(inputPrice != null) {
      size = inputPrice.getPrice(random);
    }
    input.setCount(size);

    // set random counts
    size = 1;
    if(outputPrice != null) {
      size = outputPrice.getPrice(random);
    }
    output.setCount(size);

    // only modify second stack if it exists
    ItemStack input2 = getInput2(random);
    if(!input2.isEmpty()) {
      // set the size for the second stack
      size = 1;
      // null means 1
      if(input2Price != null) {
        size = input2Price.getPrice(random);
      }

      input2.setCount(size);
    }

    // create the actual "recipe"
    recipeList.add(new MerchantRecipe(input, input2, output));
  }
}

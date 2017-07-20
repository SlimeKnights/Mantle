package slimeknights.mantle.util;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.entity.passive.EntityVillager.PriceInfo;
import net.minecraft.item.ItemStack;

public class TradeRandom extends TradeGeneric {

  // data
  private ItemStack[] inputs;
  private ItemStack[] outputs;

  /**
   * Trade a randomly chosen item item from the list for another randomly chosen item
   * @param inputs       List of inputs to randomly choose from
   * @param inputPrice   Random stack size range for input
   * @param outputs      List of outputs to randomly choose from
   * @param outputPrice  Random stack size range for output
   */
  public TradeRandom(ItemStack[] inputs, PriceInfo inputPrice, ItemStack[] outputs, PriceInfo outputPrice) {
    super(ItemStack.EMPTY, inputPrice, ItemStack.EMPTY, outputPrice);
    this.inputs = inputs;
    this.outputs = outputs;
  }

  /**
   * Trade a randomly chosen item from the list for emeralds
   * @param inputs       List of inputs to randomly choose from
   * @param inputPrice   Random stack size range for input
   * @param outputPrice  Random stack size range for output
   */
  public TradeRandom(ItemStack[] inputs, PriceInfo inputPrice, PriceInfo outputPrice) {
    this(inputs, inputPrice, null, outputPrice);
  }

  /**
   * Trade emeralds for a randomly chosen item from the list
   * @param inputPrice   Random stack size range for input
   * @param outputs      List of outputs to randomly choose from
   * @param outputPrice  Random stack size range for output
   */
  public TradeRandom(PriceInfo inputPrice, ItemStack[] outputs, PriceInfo outputPrice) {
    this(null, inputPrice, outputs, outputPrice);
  }

  @Override
  @Nonnull
  protected ItemStack getOutput(Random random) {
    // array does not exist? return default
    if(outputs == null) {
      return super.getOutput(random);
    }
    // return a random stack from the list
    int i = random.nextInt(outputs.length);
    return outputs[i].copy();
  }

  @Override
  @Nonnull
  protected ItemStack getInput(Random random) {
    // array does not exist? return default
	if(inputs == null) {
      return super.getInput(random);
	}
    // return a random stack from the list
    int i = random.nextInt(inputs.length);
    return inputs[i].copy();
  }

}

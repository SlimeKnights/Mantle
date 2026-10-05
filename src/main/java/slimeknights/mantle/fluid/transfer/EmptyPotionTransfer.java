package slimeknights.mantle.fluid.transfer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.util.DataComponentHelper;

/** Fluid transfer info that empties a fluid from a potion item, but empties water if its the water potion */
public class EmptyPotionTransfer extends EmptyFluidContainerTransfer {
  public static final RecordLoadable<EmptyPotionTransfer> LOADER = RecordLoadable.create(
    INPUT_FIELD, RESULT_FIELD,
    IntLoadable.FROM_ONE.requiredField("amount", t -> t.fluid.getAmount()),
    EmptyPotionTransfer::new);

  public EmptyPotionTransfer(Ingredient input, ItemOutput filled, int amount) {
    super(input, filled, FluidOutput.fromFluid(Fluids.WATER, amount));
  }

  @Override
  public RecordLoadable<? extends EmptyPotionTransfer> getLoader() {
    return LOADER;
  }

  @Override
  public boolean matches(ItemStack stack, FluidStack fluid) {
    // to match, must either have water in the stack, or a potion fluid
    return super.matches(stack, fluid)
      && (TagPreference.getPreference(MantleTags.Fluids.POTION).isPresent() || stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER));
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    // water just returns water
    if (stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
      return fluid.copy();
    }
    // if it's not water, we need a potion fluid to return anything
    return TagPreference.getPreference(MantleTags.Fluids.POTION)
      .map(value -> DataComponentHelper.copy(new FluidStack(value, fluid.getAmount()), stack, DataComponents.POTION_CONTENTS))
      .orElse(FluidStack.EMPTY);
  }
}

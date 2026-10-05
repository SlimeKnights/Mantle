package slimeknights.mantle.fluid.transfer;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.function.Consumer;

/** Fluid transfer info that empties a fluid from an item */
@RequiredArgsConstructor
public class EmptyFluidContainerTransfer implements IFluidContainerTransfer {
  protected static final LoadableField<Ingredient,EmptyFluidContainerTransfer> INPUT_FIELD = Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("input", t -> t.input);
  protected static final LoadableField<ItemOutput,EmptyFluidContainerTransfer> RESULT_FIELD = ItemOutput.Loadable.OPTIONAL_ITEM.emptyField("result", t -> t.result);
  protected static final LoadableField<FluidOutput,EmptyFluidContainerTransfer> FLUID_FIELD = FluidOutput.Loadable.REQUIRED.requiredField("fluid", t -> t.fluid);
  public static final RecordLoadable<EmptyFluidContainerTransfer> LOADER = RecordLoadable.create(INPUT_FIELD, RESULT_FIELD, FLUID_FIELD, EmptyFluidContainerTransfer::new);

  protected final Ingredient input;
  protected final ItemOutput result;
  protected final FluidOutput fluid;

  @Override
  public RecordLoadable<? extends EmptyFluidContainerTransfer> getLoader() {
    return LOADER;
  }

  @Override
  public void addRepresentativeItems(Consumer<Item> consumer) {
    for (ItemStack stack : input.getItems()) {
      consumer.accept(stack.getItem());
    }
  }

  @Override
  public boolean matches(ItemStack stack, FluidStack fluid) {
    return input.test(stack);
  }

  /** Gets the contained fluid in the given stack */
  protected FluidStack getFluid(ItemStack stack) {
    return fluid.get();
  }

  @Nullable
  @Override
  public TransferResult transfer(ItemStack stack, FluidStack fluid, IFluidHandler handler, TransferDirection direction) {
    if (!direction.canEmpty()) {
      return null;
    }
    FluidStack contained = getFluid(stack);
    int simulated = handler.fill(contained.copy(), FluidAction.SIMULATE);
    if (simulated == contained.getAmount()) {
      int actual = handler.fill(contained.copy(), FluidAction.EXECUTE);
      if (actual > 0) {
        if (actual != this.fluid.getAmount()) {
          Mantle.logger.error("Wrong amount filled from {}, expected {}, filled {}", BuiltInRegistries.ITEM.getKey(stack.getItem()), this.fluid.getAmount(), actual);
        }
        return new TransferResult(result.copy(), contained, false);
      }
    }
    return null;
  }
}

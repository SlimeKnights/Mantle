package slimeknights.mantle.fluid.transfer;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.function.Consumer;

/** Fluid transfer info that fills a fluid into an item */
@RequiredArgsConstructor
public class FillFluidContainerTransfer implements IFluidContainerTransfer {
  protected static final LoadableField<Ingredient,FillFluidContainerTransfer> INPUT_FIELD = Loadables.ITEM_INGREDIENT_NONEMPTY.requiredField("input", t -> t.input);
  protected static final LoadableField<ItemOutput,FillFluidContainerTransfer> RESULT_FIELD = ItemOutput.Loadable.REQUIRED_ITEM.requiredField("result", t -> t.result);
  protected static final LoadableField<SizedFluidIngredient,FillFluidContainerTransfer> FLUID_FIELD = Loadables.SIZED_FLUID_INGREDIENT.requiredField("fluid", t -> t.fluid);
  public static final RecordLoadable<FillFluidContainerTransfer> LOADER = RecordLoadable.create(INPUT_FIELD, RESULT_FIELD, FLUID_FIELD, FillFluidContainerTransfer::new);

  protected final Ingredient input;
  protected final ItemOutput result;
  protected final SizedFluidIngredient fluid;

  @Override
  public RecordLoadable<? extends FillFluidContainerTransfer> getLoader() {
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
    return input.test(stack) && this.fluid.test(fluid);
  }

  /** Gets the output filled with the given fluid */
  protected ItemStack getFilled(FluidStack drained) {
    return this.result.get().copy();
  }

  @Nullable
  @Override
  public TransferResult transfer(ItemStack stack, FluidStack fluid, IFluidHandler handler, TransferDirection direction) {
    if (!direction.canFill()) {
      return null;
    }
    int amount = this.fluid.amount();
    FluidStack toDrain = fluid.copyWithAmount(amount);
    FluidStack simulated = handler.drain(toDrain.copy(), FluidAction.SIMULATE);
    if (simulated.getAmount() == amount) {
      FluidStack actual = handler.drain(toDrain.copy(), FluidAction.EXECUTE);
      if (actual.getAmount() != amount) {
        Mantle.logger.error("Wrong amount drained from {}, expected {}, filled {}", BuiltInRegistries.ITEM.getKey(stack.getItem()), fluid.getAmount(), actual.getAmount());
      }
      return new TransferResult(getFilled(toDrain), toDrain, true);
    }
    return null;
  }
}

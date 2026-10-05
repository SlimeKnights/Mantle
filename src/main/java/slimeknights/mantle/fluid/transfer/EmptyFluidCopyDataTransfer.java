package slimeknights.mantle.fluid.transfer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.util.DataComponentHelper;

import java.util.List;

/** Fluid transfer info that empties a fluid from an item, copying the listed components to the stack */
public class EmptyFluidCopyDataTransfer extends EmptyFluidContainerTransfer {
  public static final RecordLoadable<EmptyFluidCopyDataTransfer> LOADER = RecordLoadable.create(
    INPUT_FIELD, RESULT_FIELD, FLUID_FIELD,
    Loadables.DATA_COMPONENT_TYPE.list(ArrayLoadable.COMPACT).requiredField("copy", t -> t.copyComponents),
    EmptyFluidCopyDataTransfer::new);

  private final List<DataComponentType<?>> copyComponents;
  public EmptyFluidCopyDataTransfer(Ingredient input, ItemOutput filled, FluidOutput fluid, List<DataComponentType<?>> copyComponents) {
    super(input, filled, fluid);
    this.copyComponents = copyComponents;
  }

  @Override
  public RecordLoadable<? extends EmptyFluidCopyDataTransfer> getLoader() {
    return LOADER;
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    return DataComponentHelper.copy(super.getFluid(stack).copy(), stack, copyComponents);
  }
}

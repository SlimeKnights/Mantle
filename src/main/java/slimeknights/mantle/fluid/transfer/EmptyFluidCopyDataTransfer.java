package slimeknights.mantle.fluid.transfer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.List;

/** Fluid transfer info that empties a fluid from an item, copying the listed components to the stack */
public class EmptyFluidCopyDataTransfer extends EmptyFluidContainerTransfer {
  public static final RecordLoadable<EmptyFluidCopyDataTransfer> LOADER = RecordLoadable.create(INPUT_FIELD, RESULT_FIELD, FLUID_FIELD, EmptyFluidCopyDataTransfer::new);

  public EmptyFluidCopyDataTransfer(Ingredient input, ItemOutput filled, FluidOutput fluid) {
    super(input, filled, fluid);
  }

  @Override
  public RecordLoadable<? extends EmptyFluidCopyDataTransfer> getLoader() {
    return LOADER;
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    FluidStack fluid = super.getFluid(stack);
    fluid.applyComponents(stack.getComponentsPatch());
    return fluid;
  }


  /* JEI */
  private List<FluidStack> resultFluids;

  @Override
  public List<FluidStack> resultFluids() {
    if (resultFluids == null) {
      List<ItemStack> items = inputItems();
      List<FluidStack> resultFluids = new ArrayList<>(items.size());
      for (ItemStack item : items) {
        FluidStack copy = fluid.copy();
        copy.applyComponents(item.getComponentsPatch());
        resultFluids.add(copy);
      }
      this.resultFluids = List.copyOf(resultFluids);
    }
    return resultFluids;
  }

  @Override
  public List<SlotLink> linkedSlots() {
    return List.of(SlotLink.INPUT_ITEMS, SlotLink.RESULT_FLUIDS);
  }
}

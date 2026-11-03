package slimeknights.mantle.fluid.transfer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.ArrayList;
import java.util.List;

/** Fluid transfer info that fills a fluid into an item, copying its NBT */
public class FillFluidCopyDataTransfer extends FillFluidContainerTransfer {
  public static final RecordLoadable<FillFluidCopyDataTransfer> LOADER = RecordLoadable.create(INPUT_FIELD, RESULT_FIELD, FLUID_FIELD, FillFluidCopyDataTransfer::new);

  public FillFluidCopyDataTransfer(Ingredient input, ItemOutput filled, SizedFluidIngredient fluid) {
    super(input, filled, fluid);
  }

  @Override
  public RecordLoadable<? extends FillFluidCopyDataTransfer> getLoader() {
    return LOADER;
  }

  @Override
  protected ItemStack getFilled(FluidStack drained) {
    ItemStack filled = super.getFilled(drained);
    filled.applyComponents(drained.getComponentsPatch());
    return filled;
  }


  /* JEI */
  private List<ItemStack> resultItems;

  @Override
  public List<ItemStack> resultItems() {
    if (resultItems == null) {
      List<FluidStack> fluids = inputFluids();
      List<ItemStack> resultItems = new ArrayList<>(fluids.size());
      for (FluidStack fluid : fluids) {
        ItemStack copy = result.copy();
        copy.applyComponents(fluid.getComponentsPatch());
        resultItems.add(copy);
      }
      this.resultItems = List.copyOf(resultItems);
    }
    return resultItems;
  }

  @Override
  public List<SlotLink> linkedSlots() {
    return List.of(SlotLink.INPUT_FLUIDS, SlotLink.RESULT_ITEMS);
  }
}

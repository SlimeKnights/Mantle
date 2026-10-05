package slimeknights.mantle.fluid.transfer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.util.DataComponentHelper;

import java.util.List;

/** Fluid transfer info that fills a fluid into an item, copying its NBT */
public class FillFluidCopyDataTransfer extends FillFluidContainerTransfer {
  public static final RecordLoadable<FillFluidCopyDataTransfer> LOADER = RecordLoadable.create(
    INPUT_FIELD, RESULT_FIELD, FLUID_FIELD,
    Loadables.DATA_COMPONENT_TYPE.list(ArrayLoadable.COMPACT).requiredField("copy", t -> t.copyComponents),
    FillFluidCopyDataTransfer::new);

  private final List<DataComponentType<?>> copyComponents;
  public FillFluidCopyDataTransfer(Ingredient input, ItemOutput filled, SizedFluidIngredient fluid, List<DataComponentType<?>> copyComponents) {
    super(input, filled, fluid);
    this.copyComponents = copyComponents;
  }

  @Override
  public RecordLoadable<? extends FillFluidCopyDataTransfer> getLoader() {
    return LOADER;
  }

  @Override
  protected ItemStack getFilled(FluidStack drained) {
    return DataComponentHelper.copy(super.getFilled(drained), drained, copyComponents);
  }
}

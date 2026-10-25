package slimeknights.mantle.fluid.transfer;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.function.ToIntFunction;

/** Interface for showing {@link IFluidContainerTransfer} in JEI. */
public interface IDisplayFluidTransfer {
  /** Gets a list of all input items for this transfer */
  List<ItemStack> inputItems();
  /** Gets a list of all output items for this transfer */
  List<ItemStack> resultItems();

  /** Gets a list of all input fluids for this transfer */
  List<FluidStack> inputFluids();
  /** Gets a list of all output fluids for this transfer */
  List<FluidStack> resultFluids();

  /** Gets a list of slots that should be linked. */
  default List<SlotLink> linkedSlots() {
    return List.of();
  }

  /** Enum for focus links */
  @RequiredArgsConstructor
  enum SlotLink {
    INPUT_ITEMS(r -> r.inputItems().size()),
    RESULT_ITEMS(r -> r.resultItems().size()),
    INPUT_FLUIDS(r -> r.inputFluids().size()),
    RESULT_FLUIDS(r -> r.resultFluids().size());

    private final ToIntFunction<IDisplayFluidTransfer> sizeGetter;

    /** Gets the size of this link in the recipe */
    public int getSize(IDisplayFluidTransfer transfer) {
      return sizeGetter.applyAsInt(transfer);
    }
  }

  /** Creates a new builder instance */
  static Builder builder() {
    return new Builder();
  }

  /** Builder for creating display fluid transfers */
  @Accessors(fluent = true)
  @Setter
  class Builder {
    private List<ItemStack> inputItems = List.of();
    private List<ItemStack> resultItems = List.of();
    private List<FluidStack> inputFluids = List.of();
    private List<FluidStack> resultFluids = List.of();
    private List<SlotLink> linkedSlots = List.of();

    private Builder() {}

    /** Sets the input to a single item */
    public Builder inputItem(ItemStack item) {
      return inputItems(List.of(item));
    }

    /** Sets the result to a single item */
    public Builder resultItem(ItemStack item) {
      return resultItems(List.of(item));
    }

    /** Sets the input to a single fluid */
    public Builder inputFluid(FluidStack fluid) {
      return inputFluids(List.of(fluid));
    }

    /** Sets the result to a single fluid */
    public Builder resultFluid(FluidStack fluid) {
      return resultFluids(List.of(fluid));
    }

    /** Sets the slot links */
    public Builder linkedSlots(SlotLink... slotLinks) {
      this.linkedSlots = List.of(slotLinks);
      return this;
    }

    private record DisplayFluidTransfer(List<ItemStack> inputItems, List<ItemStack> resultItems, List<FluidStack> inputFluids, List<FluidStack> resultFluids, List<SlotLink> linkedSlots) implements IDisplayFluidTransfer {}

    public IDisplayFluidTransfer build() {
      return new DisplayFluidTransfer(inputItems, resultItems, inputFluids, resultFluids, linkedSlots);
    }
  }
}

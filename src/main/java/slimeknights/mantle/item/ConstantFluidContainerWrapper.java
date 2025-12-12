package slimeknights.mantle.item;

import lombok.Getter;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

/** Represents a capability handler for a container with a constant fluid */
public class ConstantFluidContainerWrapper implements IFluidHandlerItem {
  /** Contained fluid */
  private final FluidStack fluid;
  /** If true, the container is now empty */
  private boolean empty = false;
  /** Item stack representing the current state */
  @Getter
  @Nonnull
  protected ItemStack container;
  /** Empty version of the container */
  private final ItemStack emptyStack;

  public ConstantFluidContainerWrapper(FluidStack fluid, ItemStack container, ItemStack emptyStack) {
    this.fluid = fluid;
    this.container = container;
    this.emptyStack = emptyStack;
  }

  public ConstantFluidContainerWrapper(FluidStack fluid, ItemStack container) {
    this(fluid, container, container.getCraftingRemainingItem());
  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Override
  public int getTankCapacity(int tank) {
    return fluid.getAmount();
  }

  @Override
  public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
    return stack.isEmpty() || stack.getFluid() == fluid.getFluid();
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return empty ? FluidStack.EMPTY : fluid;
  }

  @Override
  public int fill(FluidStack resource, FluidAction action) {
    return 0;
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, FluidAction action) {
    // cannot drain if: already drained, requested the wrong type, or requested too little
    if (empty || resource.getFluid() != fluid.getFluid() || resource.getAmount() < fluid.getAmount()) {
      return FluidStack.EMPTY;
    }
    if (action == FluidAction.EXECUTE) {
      container = emptyStack;
      empty = true;
    }
    return fluid.copy();
  }

  @Nonnull
  @Override
  public FluidStack drain(int maxDrain, FluidAction action) {
    // cannot drain if: already drained, requested the wrong type, or requested too little
    if (empty || maxDrain < fluid.getAmount()) {
      return FluidStack.EMPTY;
    }
    if (action == FluidAction.EXECUTE) {
      container = emptyStack;
      empty = true;
    }
    return fluid.copy();
  }
}

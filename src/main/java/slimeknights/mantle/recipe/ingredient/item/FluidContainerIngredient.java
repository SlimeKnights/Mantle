package slimeknights.mantle.recipe.ingredient.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.registration.object.FluidObject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ingredient that matches a container with exactly the given fluid.
 * Meant for matching items that behave like buckets, which are either filled or drained with no partial states.
 * @param fluid      Fluid to match. Amount must be an exact match.
 * @param display    If not empty, will display the given list of stacks as possible inputs.
 * @param container  If not empty, will fill the container with the display fluids for possible inputs.
 */
@SuppressWarnings("unused")  // API
public record FluidContainerIngredient(FluidIngredient fluid, List<ItemStack> display, ItemStack container) implements ICustomIngredient {
  public static final RecordLoadable<FluidContainerIngredient> LOADABLE = RecordLoadable.create(
    FluidIngredient.LOADABLE.requiredField("fluid", FluidContainerIngredient::fluid),
    ItemStackLoadable.REQUIRED_ITEM_DATA.list(ArrayLoadable.COMPACT_OR_EMPTY).emptyField("display", FluidContainerIngredient::display),
    ItemStackLoadable.OPTIONAL_ITEM_DATA.defaultField("container", ItemStack.EMPTY, false, FluidContainerIngredient::container),
    FluidContainerIngredient::new);

  public static final ResourceLocation ID = Mantle.getResource("fluid_container");

  /** Creates an instance from a fluid ingredient with the given stacks for display */
  public static FluidContainerIngredient withDisplay(FluidIngredient fluid, ItemStack... display) {
    return new FluidContainerIngredient(fluid, List.of(display), ItemStack.EMPTY);
  }
  /** Creates an instance from a fluid ingredient with the given items for display */
  public static FluidContainerIngredient withDisplay(FluidIngredient fluid, ItemLike... display) {
    return new FluidContainerIngredient(fluid, Arrays.stream(display).map(ItemStack::new).toList(), ItemStack.EMPTY);
  }

  /** Creates an instance that fills the given fluid container for display stacks */
  public static FluidContainerIngredient forContainer(FluidIngredient fluid, ItemStack container) {
    return new FluidContainerIngredient(fluid, List.of(), container);
  }

  /** Creates an instance that fills the given fluid container for display stacks */
  public static FluidContainerIngredient forContainer(FluidIngredient fluid, ItemLike container) {
    return forContainer(fluid, new ItemStack(container));
  }

  /** Creates an instance for 1 bucket of the given fluid, using the bucket form as the display stack. */
  public static FluidContainerIngredient fromFluid(FluidObject<?> fluid) {
    return withDisplay(fluid.ingredient(FluidType.BUCKET_VOLUME), fluid);
  }

  @Override
  public IngredientType<FluidContainerIngredient> getType() {
    return MantleRecipes.FLUID_CONTAINER.get();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    // check that we have a craft remainder item. if we don't, we don't know how to properly drain this item
    ItemStack container = stack.getCraftingRemainingItem();
    if (container.isEmpty()) {
      return false;
    }
    // need a copy of the stack with count 1 as stacked fluid containers might otherwise give wrong values
    stack = stack.copyWithCount(1);
    // must have a fluid capability with exactly 1 tank
    IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (handler == null || handler.getTanks() != 1) {
      return false;
    }
    // second, must contain exactly the requested fluid
    FluidStack fluid = handler.getFluidInTank(0);
    if (fluid.isEmpty() || !this.fluid.test(fluid.getFluid()) || this.fluid.getAmount(fluid.getFluid()) != fluid.getAmount()) {
      return false;
    }
    // alright, we know it has the fluid, the question is just whether draining the fluid will give the craft remainder
    // since we already copied we can just go ahead and drain it
    FluidStack drained = handler.drain(fluid, FluidAction.EXECUTE);
    return FluidStack.matches(drained, fluid) && ItemStack.matches(container, handler.getContainer());
  }

  @Override
  public Stream<ItemStack> getItems() {
    Stream<ItemStack> display = this.display.stream();
    if (container.isEmpty()) {
      return display;
    }
    // combine display items (might be empty) with filled container item
    return Stream.concat(
      display,
      fluid.getAllFluids().stream().map(fluid -> {
        ItemStack container = this.container.copy();
        IFluidHandlerItem handler = container.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler != null && handler.fill(fluid.copy(), FluidAction.EXECUTE) == fluid.getAmount()) {
          return handler.getContainer();
        }
        return ItemStack.EMPTY;
      }).filter(stack -> !stack.isEmpty())
    );
  }
}

package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;

/**
 * Ingredient that matches a container of fluid.
 * 
 * In NeoForge 1.21+, AbstractIngredient and IIngredientSerializer were removed.
 * This class now provides standalone fluid container matching functionality.
 * 
 * @deprecated The ingredient system has changed in 1.21+. Consider using ICustomIngredient
 *             with IngredientType for custom ingredient implementations.
 */
@Deprecated(forRemoval = true)
@SuppressWarnings("unused")  // API
public class FluidContainerIngredient {
  public static final ResourceLocation ID = Mantle.getResource("fluid_container");

  /** Ingredient to use for matching */
  private final FluidIngredient fluidIngredient;
  /** Internal ingredient to display the ingredient recipe viewers */
  @Nullable
  private final Ingredient display;
  private ItemStack[] displayStacks;
  
  protected FluidContainerIngredient(FluidIngredient fluidIngredient, @Nullable Ingredient display) {
    this.fluidIngredient = fluidIngredient;
    this.display = display;
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient, Ingredient display) {
    return new FluidContainerIngredient(ingredient, display);
  }

  /** Creates an instance from a fluid ingredient with no display, not recommended */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient) {
    return new FluidContainerIngredient(ingredient, null);
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromFluid(FluidObject<?> fluid) {
    return fromIngredient(fluid.ingredient(FluidType.BUCKET_VOLUME), Ingredient.of(fluid));
  }

  public boolean test(@Nullable ItemStack stack) {
    // first, must have a fluid capability
    if (stack == null || stack.isEmpty()) {
      return false;
    }

    IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (handler == null || handler.getTanks() != 1) {
      return false;
    }

    FluidStack contained = handler.getFluidInTank(0);
    if (contained.isEmpty() || !fluidIngredient.test(contained.getFluid())) {
      return false;
    }
    Fluid fluid = contained.getFluid();
    int amount = fluidIngredient.getAmount(fluid);
    if (amount != contained.getAmount()) {
      return false;
    }

    ItemStack copy = ItemHandlerHelper.copyStackWithSize(stack, 1);
    IFluidHandlerItem copyHandler = copy.getCapability(Capabilities.FluidHandler.ITEM);
    if (copyHandler == null) {
      return false;
    }

    FluidStack drained = copyHandler.drain(amount, FluidAction.EXECUTE);
    return drained.getFluid() == fluid && drained.getAmount() == amount && ItemStack.matches(stack.getCraftingRemainder(), copyHandler.getContainer());
  }

  public ItemStack[] getItems() {
    if (displayStacks == null) {
      // no container? unfortunately hard to display this recipe so show nothing
      if (display == null) {
        displayStacks = new ItemStack[0];
      } else {
        displayStacks = display.getItems();
      }
    }
    return displayStacks;
  }

  public boolean isSimple() {
    return false;
  }

  public boolean isEmpty() {
    return false;
  }

  /** Gets the fluid ingredient */
  public FluidIngredient getFluidIngredient() {
    return fluidIngredient;
  }

  /** Gets the display ingredient, may be null */
  @Nullable
  public Ingredient getDisplay() {
    return display;
  }
}

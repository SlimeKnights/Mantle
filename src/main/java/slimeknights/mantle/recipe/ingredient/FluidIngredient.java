package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.IAmLoadable;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.FluidStackLoadable;
import slimeknights.mantle.data.loadable.mapping.EitherLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple displayable ingredient type for fluids.
 */
@SuppressWarnings("unused")
public abstract class FluidIngredient implements IAmLoadable {
  /** Empty fluid ingredient, matching empty stacks. This ingredient does not parse from JSON, use use defaulting methods if you wish to use it */
  public static final FluidMatch EMPTY = new FluidMatch(Fluids.EMPTY, 0);


  /* Loadables */

  /** Creates a builder with set and tag */
  private static EitherLoadable.TypedBuilder<FluidIngredient> loadableBuilder() {
    return EitherLoadable.<FluidIngredient>typed().key("fluid", FLUID_MATCH).key("tag", TAG_MATCH);
  }
  /** Loadable for network writing of fluids */
  private static final Loadable<FluidIngredient> NETWORK = FluidStackLoadable.REQUIRED_STACK.list(0).flatMap(fluids -> FluidIngredient.of(fluids.stream().map(FluidIngredient::of).toList()), FluidIngredient::getFluids);
  /** Loadable for fluid matches */
  private static final RecordLoadable<FluidMatch> FLUID_MATCH = RecordLoadable.create(Loadables.FLUID.requiredField("fluid", i -> i.fluid), IntLoadable.FROM_ONE.requiredField("amount", i -> i.amount), FluidIngredient::of);
  /** Loadable for tag matches */
  private static final RecordLoadable<TagMatch> TAG_MATCH = RecordLoadable.create(Loadables.FLUID_TAG.requiredField("tag", i -> i.tag), IntLoadable.FROM_ONE.requiredField("amount", i -> i.amount), FluidIngredient::of);
  /** Loadable for tag matches */
  private static final Loadable<Compound> COMPOUND = loadableBuilder().build(NETWORK).list(2).flatMap(Compound::new, c -> c.ingredients);
  /** Loadable for any fluid ingredient */
  public static final Loadable<FluidIngredient> LOADABLE = loadableBuilder().array(COMPOUND).build(NETWORK);


  /* Constructors */

  /**
   * Creates a new ingredient using the given fluid and amount
   * @param fluid   Fluid to check
   * @param amount  Minimum fluid amount
   * @return  Fluid ingredient for this fluid
   */
  public static FluidMatch of(Fluid fluid, int amount) {
    if (fluid == Fluids.EMPTY || amount <= 0) {
      return EMPTY;
    }
    return new FluidMatch(fluid, amount);
  }

  /**
   * Creates a new ingredient using the given fluidstack
   * @param stack  Fluid stack
   * @return  Fluid ingredient for this fluid stack
   */
  public static FluidIngredient of(FluidStack stack) {
    return of(stack.getFluid(), stack.getAmount());
  }

  /**
   * Creates a new fluid ingredient from the given tag
   * @param fluid   Fluid tag
   * @param amount  Minimum fluid amount
   * @return  Fluid ingredient from a tag
   */
  public static TagMatch of(TagKey<Fluid> fluid, int amount) {
    return new TagMatch(fluid, amount);
  }

  /**
   * Creates a new compound ingredient from the given list of ingredients
   * @param ingredients  Ingredient list
   * @return  Compound ingredient
   */
  public static FluidIngredient of(FluidIngredient... ingredients) {
    return of(List.of(ingredients));
  }

  /**
   * Creates a new compound ingredient from the given list of ingredients
   * @param ingredients  Ingredient list
   * @return  Compound ingredient
   */
  public static FluidIngredient of(List<FluidIngredient> ingredients) {
    if (ingredients.size() == 1) {
      return ingredients.get(0);
    }
    return new Compound(ingredients);
  }


  /** Cached list of display fluids */
  private List<FluidStack> displayFluids;

  /**
   * Checks if the given fluid matches this ingredient
   * @param fluid  Fluid to check
   * @return  True if the fluid matches
   */
  public abstract boolean test(Fluid fluid);

  /**
   * Gets the amount of the given fluid needed for the recipe
   * @param fluid  Fluid to check
   * @return  Amount of the fluid needed
   */
  public abstract int getAmount(Fluid fluid);

  /**
   * Checks if the given fluid stack argument matches this ingredient
   * @param stack  Fluid stack to check
   * @return  True if the fluid matches this ingredient and the amount is equal or greater than this
   */
  public boolean test(FluidStack stack) {
    Fluid fluid = stack.getFluid();
    return stack.getAmount() >= getAmount(fluid) && test(stack.getFluid());
  }

  /**
   * Gets a list of fluid stacks contained in this ingredient for display
   * @return  List of fluid stacks for this ingredient
   */
  public List<FluidStack> getFluids() {
    if (displayFluids == null) {
      displayFluids = getAllFluids().stream().filter(stack -> {
        Fluid fluid = stack.getFluid();
        return fluid.isSource(fluid.defaultFluidState());
      }).collect(Collectors.toList());
    }
    return displayFluids;
  }

  /**
   * Gets a list of fluid stacks contained in this ingredient for display, may include flowing fluids
   * @return  List of fluid stacks for this ingredient
   */
  protected abstract List<FluidStack> getAllFluids();

  /**
   * Serializes the Fluid Ingredient into JSON
   * @return  FluidIngredient JSON
   */
  public JsonElement serialize() {
    return LOADABLE.serialize(this);
  }

  /** Gets the fluid ingredient from the parent and deserializes it */
  public static FluidIngredient deserialize(JsonObject parent, String key) {
    return LOADABLE.getIfPresent(parent, key);
  }

  /** @deprecated use {@link #LOADABLE} with {@link Loadable#convert(JsonElement, String)} */
  @Deprecated
  public static FluidIngredient deserialize(JsonElement element, String key) {
    return LOADABLE.convert(element, key);
  }

  /**
   * Writes the ingredient into the packet buffer
   * @param buffer Packet buffer instance
   */
  public void write(FriendlyByteBuf buffer) {
    NETWORK.encode(buffer, this);
  }

  /**
   * Reads a fluid ingredient from the packet buffer
   * @param buffer  Buffer instance
   * @return  Fluid ingredient instance
   */
  public static FluidIngredient read(FriendlyByteBuf buffer) {
    return NETWORK.decode(buffer);
  }


  /**
   * Fluid ingredient that matches a single fluid
   */
  @AllArgsConstructor(access=AccessLevel.PRIVATE)
  private static class FluidMatch extends FluidIngredient {

    private final Fluid fluid;
    private final int amount;

    @Override
    public Loadable<?> loadable() {
      return FLUID_MATCH;
    }

    @Override
    public boolean test(Fluid fluid) {
      return fluid == this.fluid;
    }

    @Override
    public int getAmount(Fluid fluid) {
      return amount;
    }

    @Override
    public List<FluidStack> getAllFluids() {
      return Collections.singletonList(new FluidStack(fluid, amount));
    }
  }

  /**
   * Fluid ingredient that matches a tag
   */
  @AllArgsConstructor
  private static class TagMatch extends FluidIngredient {
    private final TagKey<Fluid> tag;
    private final int amount;

    @Override
    public Loadable<?> loadable() {
      return TAG_MATCH;
    }

    @Override
    public boolean test(Fluid fluid) {
      return fluid.is(tag);
    }

    @Override
    public int getAmount(Fluid fluid) {
      return amount;
    }

    @Override
    public List<FluidStack> getAllFluids() {
      return RegistryHelper.getTagValueStream(Registry.FLUID, tag)
                          .map(fluid -> new FluidStack(fluid, amount))
                          .toList();
    }
  }

  /**
   * Fluid ingredient that matches a list of ingredients
   */
  @RequiredArgsConstructor
  private static class Compound extends FluidIngredient {
    private final List<FluidIngredient> ingredients;

    @Override
    public Loadable<?> loadable() {
      return COMPOUND;
    }

    @Override
    public boolean test(Fluid fluid) {
      for (FluidIngredient ingredient : ingredients) {
        if (ingredient.test(fluid)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean test(FluidStack stack) {
      for (FluidIngredient ingredient : ingredients) {
        if (ingredient.test(stack)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public int getAmount(Fluid fluid) {
      for (FluidIngredient ingredient : ingredients) {
        if (ingredient.test(fluid)) {
          return ingredient.getAmount(fluid);
        }
      }
      return 0;
    }

    @Override
    public List<FluidStack> getAllFluids() {
      return ingredients.stream()
                        .flatMap(ingredient -> ingredient.getFluids().stream())
                        .collect(Collectors.toList());
    }
  }
}

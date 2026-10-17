package slimeknights.mantle.recipe.ingredient;

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.network.codec.StreamableCodec;

import java.util.List;

/** Helpers for working with ingredients. Largely bringing back lost but useful behavior from the removed Mantle ingredients. */
public class IngredientHelper {
  private IngredientHelper() {}

  /** Creates a new sized ingredient from the given items with the given size. */
  public static SizedIngredient sized(int size, ItemLike... items) {
    return new SizedIngredient(Ingredient.of(items), size);
  }

  /** Creates a new sized ingredient from the given stack. */
  public static SizedIngredient sized(ItemStack stack) {
    return new SizedIngredient(Ingredient.of(stack), stack.getCount());
  }

  /** Creates an ingredient type from the given loadable. */
  public static <T extends ICustomIngredient> IngredientType<T> itemType(RecordLoadable<T> loadable) {
    return new IngredientType<>(
      new LoadableMapCodec<>(loadable),
      new StreamableCodec<>(loadable)
    );
  }

  /** Creates an ingredient type from the given loadable. */
  public static <T extends FluidIngredient> FluidIngredientType<T> fluidType(RecordLoadable<T> loadable) {
    return new FluidIngredientType<>(
      new LoadableMapCodec<>(loadable),
      new StreamableCodec<>(loadable)
    );
  }

  /** Checks if the given fluid is a source (that is, not flowing). */
  public static boolean isSource(Fluid fluid) {
    return fluid.isSource(fluid.defaultFluidState());
  }

  /**
   * Checks if the given fluid stack is a source (that is, not flowing).
   * Used to filter fluids returned by {@link FluidIngredient} and {@link net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient} in focus linked recipes as JEI hides such fluids.
   */
  public static boolean isSource(FluidStack stack) {
    return !stack.isEmpty() && isSource(stack.getFluid());
  }

  /** Creates a holder set from the list of items */
  @SuppressWarnings("deprecation")
  public static HolderSet<Item> itemSet(List<? extends ItemLike> items) {
    return HolderSet.direct(items.stream().map(item -> item.asItem().builtInRegistryHolder()).toList());
  }

  /** Creates a holder set from the list of fluids */
  @SuppressWarnings("deprecation")
  public static HolderSet<Fluid> fluidSet(List<? extends Fluid> fluids) {
    return HolderSet.direct(fluids.stream().map(Fluid::builtInRegistryHolder).toList());
  }

  /** Creates a holder set from the given tag */
  @SuppressWarnings("deprecation")
  public static <T> HolderSet<T> tagSet(Registry<T> registry, TagKey<T> tag) {
    return HolderSet.emptyNamed(registry.holderOwner(), tag);
  }

  /** Creates a holder set from the given item tag */
  public static HolderSet<Item> itemSet(TagKey<Item> tag) {
    return tagSet(BuiltInRegistries.ITEM, tag);
  }

  /** Creates a holder set from the given fluid tag */
  public static HolderSet<Fluid> fluidSet(TagKey<Fluid> tag) {
    return tagSet(BuiltInRegistries.FLUID, tag);
  }
}

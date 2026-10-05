package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.data.loadable.LoadableCodec;
import slimeknights.mantle.data.loadable.StreamableCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Helpers for creating ingredients. Largely bringing back lost but useful constructors from the removed Mantle ingredients. */
public class IngredientHelper {
  /** Creates a new sized ingredient from the given items with the given size. */
  public static SizedIngredient sized(int size, ItemLike... items) {
    return new SizedIngredient(Ingredient.of(items), size);
  }

  /** Creates a new sized ingredient from the given stack. */
  public static SizedIngredient sized(ItemStack stack) {
    return new SizedIngredient(Ingredient.of(stack), stack.getCount());
  }

  /** Creates an ingredient type from the given loadable. */
  public static <T extends ICustomIngredient> IngredientType<T> ingredientType(RecordLoadable<T> loadable) {
    return new IngredientType<>(
      MapCodec.assumeMapUnsafe(new LoadableCodec<>(loadable)),
      new StreamableCodec<>(loadable)
    );
  }
}

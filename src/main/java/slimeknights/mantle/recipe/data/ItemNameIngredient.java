package slimeknights.mantle.recipe.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;

/**
 * Helper for datagen to create an ingredient referencing items from other mods by name.
 * In 1.21+, the ingredient system has changed significantly - Ingredient.Value and custom
 * serializers are no longer supported in the same way.
 * 
 * This class is now a simple data holder for item names, used in datagen contexts.
 * Should never be used outside datagen.
 * 
 * @deprecated This class is being phased out. In 1.21+, consider using standard Ingredient
 *             construction with registered items or crafting JSON directly.
 */
@Deprecated(forRemoval = true)
public class ItemNameIngredient {
  private final List<ResourceLocation> names;
  
  protected ItemNameIngredient(List<ResourceLocation> names) {
    this.names = names;
  }

  /** Creates a new ingredient from a list of names */
  public static ItemNameIngredient from(List<ResourceLocation> names) {
    return new ItemNameIngredient(names);
  }

  /** Creates a new ingredient from a list of names */
  public static ItemNameIngredient from(ResourceLocation... names) {
    return from(Arrays.asList(names));
  }

  /** Gets the list of item names */
  public List<ResourceLocation> getNames() {
    return names;
  }
}

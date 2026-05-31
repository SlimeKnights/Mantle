package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ingredient for a non-NBT sensitive item from another mod, should never be used outside datagen
 */
public final class ItemNameIngredient {
  private ItemNameIngredient() {}

  /** Creates a new ingredient from a list of names */
  public static Ingredient from(List<ResourceLocation> names) {
    return new NamedItemIngredient(List.copyOf(names)).toVanilla();
  }

  /** Creates a new ingredient from a list of names */
  public static Ingredient from(ResourceLocation... names) {
    return from(Arrays.asList(names));
  }

  /** Creates a JSON object for a name */
  private static JsonObject forName(ResourceLocation name) {
    JsonObject json = new JsonObject();
    json.addProperty("item", name.toString());
    return json;
  }

  /** Serializes a datagen-only named item ingredient, or returns null if the ingredient is not one. */
  public static JsonElement serialize(Ingredient ingredient) {
    if (ingredient.getCustomIngredient() instanceof NamedItemIngredient named) {
      return named.toJson();
    }
    if (ingredient.getCustomIngredient() instanceof CompoundIngredient compound) {
      JsonArray array = new JsonArray();
      for (Ingredient child : compound.children()) {
        JsonElement childJson = serialize(child);
        array.add(childJson != null ? childJson : IngredientLoadable.DISALLOW_EMPTY.serialize(child));
      }
      return array;
    }
    return null;
  }

  private record NamedItemIngredient(List<ResourceLocation> names) implements ICustomIngredient {
    private JsonElement toJson() {
      if (names.size() == 1) {
        return forName(names.get(0));
      }
      JsonArray array = new JsonArray();
      for (ResourceLocation name : names) {
        array.add(forName(name));
      }
      return array;
    }

    @Override
    public boolean test(ItemStack stack) {
      ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
      return names.contains(id);
    }

    @Override
    public Stream<ItemStack> getItems() {
      return names.stream()
                  .flatMap(name -> BuiltInRegistries.ITEM.getOptional(name).stream())
                  .map(ItemStack::new);
    }

    @Override
    public boolean isSimple() {
      return true;
    }

    @Override
    public IngredientType<?> getType() {
      throw new UnsupportedOperationException("ItemNameIngredient is only supported by Mantle datagen serializers");
    }
  }
}

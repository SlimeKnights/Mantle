package slimeknights.mantle.recipe.data.mock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.List;

/**
 * Interface for serializing recipes without using their builder, to avoid a compile time dependency.
 * @see MockRecipeProvider
 */
public interface MockRecipe {
  /** Gets the ID for the recipe. */
  ResourceLocation id();

  /** Serializes the recipe to JSON */
  JsonObject serializeRecipe(Serializer serializer);

  /** Extension of MockRecipe that mirrors the previous finished recipe interface. */
  interface SimpleMockRecipe extends MockRecipe {
    /** Gets the ID for the recipe serializer */
    ResourceLocation serializerId();

    /** Serialize any recipe specific fields. */
    void serializeRecipeData(JsonObject json, Serializer serializer);

    /** Serializes the recipe to JSON */
    @Override
    default JsonObject serializeRecipe(Serializer serializer) {
      JsonObject json = new JsonObject();
      json.addProperty("type", serializerId().toString());
      serializeRecipeData(json, serializer);
      return json;
    }
  }


  /* Helper for serializing using registry ops and codecs. */
  record Serializer(RegistryOps<JsonElement> ops, TypedMap context, HolderLookup.Provider registries) {
    public Serializer(HolderLookup.Provider registries) {
      this(registries.createSerializationContext(JsonOps.INSTANCE), ContextKey.lookupContext(registries).build(), registries);
    }


    /* General */

    /** Serializes the given element */
    public <T> JsonElement serialize(Codec<T> codec, T object) {
      return JsonHelper.serialize(ops, codec, object);
    }

    /** Serializes the given list */
    public <T> JsonArray serializeList(Codec<T> codec, List<T> objects) {
      return JsonHelper.serializeList(ops, codec, objects);
    }

    /** Serializes the given array */
    @SafeVarargs
    public final <T> JsonArray serializeArray(Codec<T> codec, T... objects) {
      return JsonHelper.serializeArray(ops, codec, objects);
    }

    /** Serializes the given element */
    public <T> JsonElement serialize(Loadable<T> loadable, T object) {
      return loadable.serialize(object, context);
    }


    /* Common components */

    /** Adds the list of conditions to the root recipe json at the proper key. */
    public void addConditions(JsonObject json, ICondition... conditions) {
      if (conditions.length > 0) {
        json.add("neoforge:conditions", serializeArray(ICondition.CODEC, conditions));
      }
    }

    /** Serializes a required item ingredient */
    public JsonElement ingredient(Ingredient ingredient) {
      return serialize(Ingredient.CODEC_NONEMPTY, ingredient);
    }

    /** Serializes a sized item ingredient */
    public JsonElement ingredient(SizedIngredient ingredient) {
      return serialize(SizedIngredient.FLAT_CODEC, ingredient);
    }

    /** Serializes a required fluid ingredient */
    public JsonElement fluidIngredient(FluidIngredient ingredient) {
      return serialize(FluidIngredient.CODEC_NON_EMPTY, ingredient);
    }

    /** Serializes a sized fluid ingredient */
    public JsonElement fluidIngredient(SizedFluidIngredient ingredient) {
      return serialize(SizedFluidIngredient.FLAT_CODEC, ingredient);
    }

    /** Serializes a required item output */
    public JsonElement item(ItemOutput output) {
      return serialize(ItemOutput.REQUIRED_ITEM_CODEC, output);
    }

    /** Serializes a required item stack output */
    public JsonElement stack(ItemOutput output) {
      return serialize(ItemOutput.REQUIRED_STACK_CODEC, output);
    }

    /** Serializes a required fluid output */
    public JsonElement fluid(FluidOutput output) {
      return serialize(FluidOutput.REQUIRED_CODEC, output);
    }
  }
}

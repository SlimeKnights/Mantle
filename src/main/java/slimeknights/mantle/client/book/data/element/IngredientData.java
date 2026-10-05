package slimeknights.mantle.client.book.data.element;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.recipe.ingredient.IngredientHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Represents an item or list of items parsed from an ingredient */
public class IngredientData implements IDataElement {
  public SizedIngredient[] ingredients = new SizedIngredient[0];
  public String action;

  private transient String error;
  @Getter
  private transient NonNullList<ItemStack> items;
  private transient boolean customData;

  public static IngredientData getItemStackData(ItemStack stack) {
    IngredientData data = new IngredientData();
    data.items = NonNullList.withSize(1, stack);
    data.customData = true;

    return data;
  }

  public static IngredientData getItemStackData(NonNullList<ItemStack> items) {
    IngredientData data = new IngredientData();
    data.items = items;
    data.customData = true;

    return data;
  }

  @Override
  public void load(BookRepository source) {
    if (this.customData) {
      return;
    }

    ArrayList<ItemStack> stacks = new ArrayList<>();
    for(SizedIngredient ingredient : ingredients) {
      if(ingredient == null) {
        continue;
      }

      Collections.addAll(stacks, ingredient.getItems());
    }

    if(ingredients == null || stacks.isEmpty() || !StringUtil.isNullOrEmpty(error)) {
      items = NonNullList.withSize(1, getMissingItem());
      return;
    }

    items = NonNullList.of(getMissingItem(), stacks.toArray(new ItemStack[0]));
  }

  private ItemStack getMissingItem() {
    return getMissingItem(this.error);
  }

  private ItemStack getMissingItem(String error) {
    ItemStack missingItem = new ItemStack(Items.BARRIER);

    // add the error as the stack name and lore
    missingItem.set(DataComponents.ITEM_NAME, Component.literal("Error Loading Item").withStyle(ChatFormatting.RESET));
    if(!StringUtil.isNullOrEmpty(error)) {
      missingItem.set(DataComponents.LORE, new ItemLore(List.of(
        Component.literal("Error:").withStyle(ChatFormatting.RESET, ChatFormatting.YELLOW),
        Component.literal(error).withStyle(ChatFormatting.RESET, ChatFormatting.YELLOW)
      )));
    }

    return missingItem;
  }

  public static class Deserializer implements JsonDeserializer<IngredientData> {
    @Override
    public IngredientData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext jsonContext) throws JsonParseException {
      IngredientData data = new IngredientData();
      TypedMap context = ContextKey.registryContext(SafeClientAccess.getRegistryAccess());

      // array - parse each element as an ingredient
      if (json.isJsonArray()) {
        JsonArray array = json.getAsJsonArray();
        data.ingredients = new SizedIngredient[array.size()];

        for (int i = 0; i < array.size(); i++) {
          try {
            data.ingredients[i] = readIngredient(array.get(i), "ingredient[" + i + ']', context);
          } catch (Exception e) {
            data.ingredients[i] = IngredientHelper.sized(data.getMissingItem(e.getMessage()));
          }
        }

        return data;
      }

      // otherwise, parse as a single ingredient
      try {
        data.ingredients = new SizedIngredient[]{ readIngredient(json, "ingredient", context) };
      } catch (Exception e) {
        data.error = e.getMessage();
        return data;
      }

      // if it's an object, also fetch the action
      if (json.isJsonObject()) {
        JsonObject object = json.getAsJsonObject();
        if (object.has("action")) {
          JsonElement action = object.get("action");
          if (action.isJsonPrimitive()) {
            JsonPrimitive primitive = action.getAsJsonPrimitive();
            if (primitive.isString()) {
              data.action = primitive.getAsString();
            }
          }
        }
      }

      return data;
    }

    /** Reads the ingredient from a json element */
    private SizedIngredient readIngredient(JsonElement json, String key, TypedMap context) {
      if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
        return SizedIngredient.of(Loadables.ITEM.parseString(json.getAsString(), key, context), 1);
      }
      if (!json.isJsonObject()) {
        throw new JsonParseException("Must be an array, string or JSON object");
      }
      JsonObject object = json.getAsJsonObject();
      return Loadables.SIZED_ITEM_INGREDIENT.convert(object, key, context);
    }
  }
}

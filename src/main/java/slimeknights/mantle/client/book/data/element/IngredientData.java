package slimeknights.mantle.client.book.data.element;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import slimeknights.mantle.client.book.repository.BookRepository;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class IngredientData implements IDataElement {
  public SizedIngredient[] ingredients = new SizedIngredient[0];
  public String action;

  private transient String error;
  private transient NonNullList<ItemStack> items;
  private transient boolean customData;

  public NonNullList<ItemStack> getItems() {
    return this.items;
  }

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

      stacks.addAll(ingredient.getMatchingStacks());
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

    missingItem.set(DataComponents.CUSTOM_NAME, Component.literal("Error Loading Item"));
    ArrayList<Component> lore = new ArrayList<>();
    if(!StringUtil.isNullOrEmpty(error)) {
      lore.add(Component.literal("Error:").withStyle(ChatFormatting.YELLOW));
      lore.add(Component.literal(error).withStyle(ChatFormatting.YELLOW));
    }
    missingItem.set(DataComponents.LORE, new ItemLore(lore));

    return missingItem;
  }

  public static class Deserializer implements JsonDeserializer<IngredientData> {
    @Override
    public IngredientData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      IngredientData data = new IngredientData();

      if(json.isJsonArray()) {
        JsonArray array = json.getAsJsonArray();
        data.ingredients = new SizedIngredient[array.size()];

        for(int i = 0; i < array.size(); i++) {
          try {
            data.ingredients[i] = readIngredient(array.get(i));
          } catch (Exception e) {
            data.ingredients[i] = SizedIngredient.of(Ingredient.of(data.getMissingItem(e.getMessage())));
          }
        }

        return data;
      }

      try {
        data.ingredients = new SizedIngredient[]{ readIngredient(json) };
      } catch (Exception e) {
        data.error = e.getMessage();
        return data;
      }

      if(json.isJsonObject()) {
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

    private SizedIngredient readIngredient(JsonElement json) {
      if(json.isJsonPrimitive()) {
        JsonPrimitive primitive = json.getAsJsonPrimitive();

        if(primitive.isString()) {
          Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(primitive.getAsString()));
          return SizedIngredient.fromItems(item);
        }
      }

      if(!json.isJsonObject()) {
        throw new JsonParseException("Must be an array, string or JSON object");
      }

      JsonObject object = json.getAsJsonObject();
      return SizedIngredient.deserialize(object);
    }
  }
}

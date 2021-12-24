package slimeknights.mantle.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;

import java.lang.reflect.Type;
import java.util.List;

/** Loot condition requiring one of the existing items is the given stack */
@RequiredArgsConstructor
public class ContainsItemModifierLootCondition implements ILootModifierCondition {
  public static final ResourceLocation ID = Mantle.getResource("contains_item");
  private final Ingredient ingredient;
  private final int amountNeeded;

  public ContainsItemModifierLootCondition(Ingredient ingredient) {
    this(ingredient, 1);
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    int matched = 0;
    for (ItemStack stack : generatedLoot) {
      if (ingredient.test(stack)) {
        matched += stack.getCount();
        if (matched >= amountNeeded) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    json.addProperty("type", ID.toString());
    json.add("ingredient", ingredient.toJson());
    if (amountNeeded != 1) {
      json.addProperty("needed", amountNeeded);
    }
    return json;
  }

  /** Parses this from JSON */
  public static ContainsItemModifierLootCondition deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject json = GsonHelper.convertToJsonObject(element, "condition");
    Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
    int needed = GsonHelper.getAsInt(json, "needed", 1);
    return new ContainsItemModifierLootCondition(ingredient, needed);
  }
}

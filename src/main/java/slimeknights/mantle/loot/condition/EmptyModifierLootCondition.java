package slimeknights.mantle.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;

import java.lang.reflect.Type;
import java.util.List;

/** Loot condition to check if previously generated loot is empty */
public class EmptyModifierLootCondition implements ILootModifierCondition, JsonDeserializer<EmptyModifierLootCondition> {
  public static final ResourceLocation ID = Mantle.getResource("empty");
  public static final EmptyModifierLootCondition INSTANCE = new EmptyModifierLootCondition();

  private EmptyModifierLootCondition() {}

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    return generatedLoot.isEmpty();
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    json.addProperty("type", ID.toString());
    return json;
  }

  @Override
  public EmptyModifierLootCondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return this;
  }
}

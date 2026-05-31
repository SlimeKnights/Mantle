package slimeknights.mantle.compat.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

public interface Serializer<T> {
  void serialize(JsonObject json, T value, JsonSerializationContext context);

  T deserialize(JsonObject json, JsonDeserializationContext context);
}

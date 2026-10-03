package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Loadable for {@link Vector3f}. Supports reading as an array or a JSON object. */
public enum Vector3fLoadable implements RecordLoadable<Vector3f> {
  INSTANCE;

  @Override
  public Vector3f convert(JsonElement element, String key, TypedMap context) {
    if (element.isJsonArray()) {
      JsonArray array = element.getAsJsonArray();
      if (array.size() != 3) {
        throw new JsonParseException("Expected " + key + " to be size 3, found " + array.size());
      }
      return new Vector3f(
        GsonHelper.convertToFloat(array.get(0), key + "[0]"),
        GsonHelper.convertToFloat(array.get(1), key + "[1]"),
        GsonHelper.convertToFloat(array.get(2), key + "[2]")
      );
    }
    return deserialize(GsonHelper.convertToJsonObject(element, key), context);
  }

  @Override
  public Vector3f deserialize(JsonObject json, TypedMap context) {
    return new Vector3f(
      GsonHelper.getAsFloat(json, "x"),
      GsonHelper.getAsFloat(json, "y"),
      GsonHelper.getAsFloat(json, "z")
    );
  }

  @Override
  public JsonElement serialize(Vector3f vector) {
    JsonArray array = new JsonArray();
    array.add(vector.x());
    array.add(vector.y());
    array.add(vector.z());
    return array;
  }

  @Override
  public void serialize(Vector3f vector, JsonObject json) {
    if (vector.x != 0) json.addProperty("x", vector.x);
    if (vector.y != 0) json.addProperty("y", vector.y);
    if (vector.z != 0) json.addProperty("z", vector.z);
  }

  @Override
  public Vector3f decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return buffer.readVector3f();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, Vector3f value) {
    buffer.writeVector3f(value);
  }
}

package slimeknights.mantle.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import slimeknights.mantle.Mantle;

/**
 * Simple implementation of a codec mapping to a JSON serializer.
 * @param <T> Object type
 */
public interface JsonCodec<T> extends Codec<T> {
  /**
   * Deserializes the object from JSON
   */
  T deserialize(JsonElement element);

  /**
   * Serializes the element to json
   */
  JsonElement serialize(T object);

  /** Gets the name of the type this parses for display in codec errors */
  default String codecError() {
    return toString();
  }

  @Override
  default <O> DataResult<Pair<T,O>> decode(DynamicOps<O> ops, O input) {
    try {
      return DataResult.success(Pair.of(deserialize(ops.convertTo(JsonOps.INSTANCE, input)), input));
    } catch (JsonParseException e) {
      Mantle.logger.warn("Unable to decode {}", codecError(), e);
      return DataResult.error(e::getMessage);
    }
  }

  @Override
  default <O> DataResult<O> encode(T input, DynamicOps<O> ops, O prefix) {
    try {
      return DataResult.success(JsonOps.INSTANCE.convertTo(ops, serialize(input)));
    } catch (JsonParseException e) {
      Mantle.logger.warn("Unable to encode {}", codecError(), e);
      return DataResult.error(e::getMessage);
    }
  }

  /** Creates a codec for a GSON element with an existing GSON serializer */
  record GsonCodec<T>(String name, Gson gson, Class<T> classType) implements JsonCodec<T> {
    @Override
    public T deserialize(JsonElement element) {
      return gson.fromJson(element, classType);
    }

    @Override
    public JsonElement serialize(T object) {
      return gson.toJsonTree(object, classType);
    }

    @Override
    public String toString() {
      return name;
    }
  }
}

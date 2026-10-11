package slimeknights.mantle.data.codec;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import slimeknights.mantle.Mantle;

import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * Implementation of {@link MapCodec} from a JSON serializer.
 * Based on {@link MapCodec#assumeMapUnsafe(Codec)}.
 * @see JsonCodec
 */
public abstract class JsonMapCodec<T> extends MapCodec<T> {
  private static final String COMPRESSED_VALUE_KEY = "compressed";

  /** Deserializes the object from JSON */
  protected abstract T deserialize(JsonObject json, DynamicOps<?> ops);

  /** Serializes the element to json */
  protected abstract JsonObject serialize(T object, DynamicOps<?> ops);

  /** Gets the name of the type this parses for display in codec errors */
  protected String codecError() {
    return toString();
  }

  @Override
  public <O> Stream<O> keys(final DynamicOps<O> ops) {
    // since we don't know the keys used by the encoder, store the whole object in a single key
    return Stream.of(ops.createString(COMPRESSED_VALUE_KEY));
  }

  @Override
  public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
    JsonElement json;
    if (ops.compressMaps()) {
      json = ops.convertTo(JsonOps.INSTANCE, input.get(COMPRESSED_VALUE_KEY));
    } else {
      json = ops.convertMap(JsonOps.INSTANCE, ops.createMap(input.entries()));
    }
    try {
      return DataResult.success(deserialize(json.getAsJsonObject(), ops));
    } catch (JsonParseException|ClassCastException e) {
      Mantle.logger.warn("Unable to decode {}", codecError(), e);
      return DataResult.error(e::getMessage);
    }
  }

  @Override
  public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
    JsonObject json;
    try {
      json = serialize(input, ops);
    } catch (RuntimeException e) {
      Mantle.logger.warn("Unable to encode {}", codecError(), e);
      DataResult<O> error = DataResult.error(e::getMessage);
      return ops.compressMaps() ? prefix.add(COMPRESSED_VALUE_KEY, error) : prefix.withErrorsFrom(error);
    }
    if (ops.compressMaps()) {
      prefix.add(COMPRESSED_VALUE_KEY, JsonOps.INSTANCE.convertTo(ops, json));
    }
    for (Entry<String, JsonElement> entry : json.entrySet()) {
      prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
    }
    return prefix;
  }
}

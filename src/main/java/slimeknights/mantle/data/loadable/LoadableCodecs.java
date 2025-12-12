package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Helpers to bridge Mantle {@link RecordLoadable} to Mojang/NeoForge codecs. */
public final class LoadableCodecs {
  private LoadableCodecs() {}

  /** Wraps a {@link RecordLoadable} as a {@link MapCodec} for NeoForge registries. */
  public static <T> MapCodec<T> mapCodec(RecordLoadable<T> loadable) {
    return new MapCodec<>() {
      @Override
      public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
        try {
          JsonObject json = new JsonObject();
          for (Pair<O,O> entry : input.entries().toList()) {
            // key is always a string for JSON/map codecs
            String key = ops.getStringValue(entry.getFirst()).result().orElse(null);
            if (key != null) {
              JsonElement value = ops.convertTo(JsonOps.INSTANCE, entry.getSecond());
              json.add(key, value);
            }
          }
          return DataResult.success(loadable.deserialize(json));
        } catch (RuntimeException e) {
          return DataResult.error(() -> e.getMessage());
        }
      }

      @Override
      public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
        JsonObject json = new JsonObject();
        loadable.serialize(input, json);
        for (var entry : json.entrySet()) {
          prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
        }
        return prefix;
      }

      @Override
      public String toString() {
        return "LoadableMapCodec[" + loadable + "]";
      }
    };
  }
}


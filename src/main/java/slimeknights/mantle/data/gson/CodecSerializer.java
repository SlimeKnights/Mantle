package slimeknights.mantle.data.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.util.JsonHelper;

import java.lang.reflect.Type;

/** Bridge between {@link Codec} and {@link com.google.gson.Gson} */
public record CodecSerializer<T>(Codec<T> codec) implements JsonSerializer<T>, JsonDeserializer<T> {
  /** Serializer instance for NeoForge conditions */
  public static final CodecSerializer<ICondition> CONDITION = new CodecSerializer<>(ICondition.CODEC);

  @Override
  public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return JsonHelper.parse(codec, json);
  }

  @Override
  public JsonElement serialize(T value, Type typeOfSrc, JsonSerializationContext context) {
    return JsonHelper.serialize(codec, value);
  }
}

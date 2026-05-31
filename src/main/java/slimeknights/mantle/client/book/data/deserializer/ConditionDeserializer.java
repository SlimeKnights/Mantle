package slimeknights.mantle.client.book.data.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.lang.reflect.Type;

public class ConditionDeserializer implements JsonDeserializer<ICondition> {
  @Override
  public ICondition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if(!json.isJsonObject())
      throw new JsonParseException("A condition must be a JSON Object");

    return ICondition.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(JsonParseException::new);
  }
}

package slimeknights.mantle.data.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.util.JsonHelper;

import java.lang.reflect.Type;

/** Serializer for a generic tag key type. */
@RequiredArgsConstructor
public class TagKeySerializer<T> implements JsonSerializer<TagKey<T>>, JsonDeserializer<TagKey<T>> {
  private final ResourceKey<Registry<T>> registry;

  @Override
  public TagKey<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return TagKey.create(registry, JsonHelper.convertToResourceLocation(json, "tag"));
  }

  @Override
  public JsonElement serialize(TagKey<T> src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.location().toString());
  }
}

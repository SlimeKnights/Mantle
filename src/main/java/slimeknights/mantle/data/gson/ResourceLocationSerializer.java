package slimeknights.mantle.data.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

/** Extension to Resource Location serializer to change the default mod ID. */
public record ResourceLocationSerializer<T extends ResourceLocation>(
  BiFunction<String, String, T> constructor,
  String modId
) implements JsonDeserializer<T>, JsonSerializer<T> {
  /** Creates an instance for resource locations */
  public static ResourceLocationSerializer<ResourceLocation> resourceLocation(String modId) {
    return new ResourceLocationSerializer<>(ResourceLocation::fromNamespaceAndPath, modId);
  }

  @Override
  public JsonElement serialize(ResourceLocation loc, Type type, JsonSerializationContext context) {
    return new JsonPrimitive(loc.toString());
  }

  @Override
  public T deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    String location = GsonHelper.convertToString(element, "location");
    // if no :, use default namespace
    int index = location.indexOf(':');
    if (index == -1) {
      return constructor.apply(modId, location);
    }
    String path = location.substring(index);
    // if empty string before :, use default namespace after trimming :
    if (index > 0) {
      return constructor.apply(location.substring(0, index), path);
    } else {
      return constructor.apply(modId, path);
    }
  }
}

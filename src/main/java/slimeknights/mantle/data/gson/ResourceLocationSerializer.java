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

/** Extension to Resource Location serializer to change the default mod ID. */
public record ResourceLocationSerializer(String modId) implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
  @Override
  public JsonElement serialize(ResourceLocation loc, Type type, JsonSerializationContext context) {
    return new JsonPrimitive(loc.toString());
  }

  @Override
  public ResourceLocation deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    String location = GsonHelper.convertToString(element, "location");
    // if no :, use default namespace
    int index = location.indexOf(':');
    if (index == -1) {
      return ResourceLocation.fromNamespaceAndPath(modId, location);
    }
    String path = location.substring(index + 1);
    // if empty string before :, use default namespace after trimming :
    if (index > 0) {
      return ResourceLocation.fromNamespaceAndPath(location.substring(0, index), path);
    } else {
      return ResourceLocation.fromNamespaceAndPath(modId, path);
    }
  }
}

package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import slimeknights.mantle.data.JsonCodec;

/** Implementation of a codec using a loadable. Note this will be inefficient comparatively when using {@link net.minecraft.nbt.NbtOps} */
public record LoadableCodec<T>(Loadable<T> loadable) implements JsonCodec<T> {
  @Override
  public T deserialize(JsonElement element) {
    return loadable.convert(element, "codec");
  }

  @Override
  public JsonElement serialize(T object) {
    return loadable.serialize(object);
  }
}

package slimeknights.mantle.data.loadable;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import slimeknights.mantle.data.loadable.mapping.DispatchLoadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Building block for {@link Loadable}, handling the serializing. Used in {@link CompositeLoadable} and {@link DispatchLoadable} */
@FunctionalInterface
public interface SimpleJsonSerializer<T> {
  /**
   * Writes the passed object to json
   * @param object  Object to serialize
   * @param context Additional serialization context, used notably to pass in {@link net.minecraft.core.HolderLookup.Provider} for vanilla registries.
   * @return  Serialized object
   * @throws RuntimeException  If unable to serialize the object
   */
  JsonElement serialize(T object, TypedMap context);

  /**
   * Writes the passed object to json
   * @param object  Object to serialize
   * @return  Serialized object
   * @throws RuntimeException  If unable to serialize the object
   */
  @NonExtendable
  default JsonElement serialize(T object) {
    return serialize(object, TypedMap.EMPTY);
  }
}

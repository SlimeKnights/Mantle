package slimeknights.mantle.data.loadable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.field.NamedField;
import slimeknights.mantle.data.loadable.field.NullableField;
import slimeknights.mantle.data.loadable.mapping.ListLoadable;
import slimeknights.mantle.data.loadable.mapping.MappedLoadable;
import slimeknights.mantle.data.loadable.mapping.SetLoadable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/** Interface for a generic loadable object */
@SuppressWarnings("unused")  // API
public interface Loadable<T> extends JsonDeserializer<T>, JsonSerializer<T> {
  /** Deserializes the object from json */
  T convert(JsonElement element, String key) throws JsonSyntaxException;

  /**
   * Gets then deserializes the given field
   * @param parent  Parent to fetch field from
   * @param key     Field to get
   * @return  Value
   */
  default T getAndDeserialize(JsonObject parent, String key) {
    if (parent.has(key)) {
      return convert(parent.get(key), key);
    }
    throw new JsonSyntaxException("Missing JSON field " + key + "");
  }

  /** Writes this object to json */
  JsonElement serialize(T object) throws RuntimeException;

  /** Reads the object from the packet buffer */
  T fromNetwork(FriendlyByteBuf buffer) throws DecoderException;

  /** Writes this object to the packet buffer */
  void toNetwork(T object, FriendlyByteBuf buffer) throws EncoderException;


  /* GSON methods, lets us easily use loadables with GSON adapters */

  @Override
  default T deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
    return convert(json, type.getTypeName());
  }

  @Override
  default JsonElement serialize(T object, Type type, JsonSerializationContext context) {
    return serialize(object);
  }


  /* Fields */

  /** Creates a required field from this loadable */
  default <P> LoadableField<T,P> field(String key, Function<P,T> getter) {
    return new NamedField<>(this, key, false, getter);
  }

  /** Creates an optional field that falls back to null */
  default <P> LoadableField<T,P> nullableField(String key, Function<P,T> getter) {
    return new NullableField<>(this, key, getter);
  }

  /** Creates a defaulting field that uses a default value when missing */
  default <P> LoadableField<T,P> defaultField(String key, T defaultValue, boolean serializeDefault, Function<P,T> getter) {
    return new DefaultingField<>(this, key, defaultValue, serializeDefault, getter);
  }

  /** Creates a defaulting field that uses a default value when missing */
  default <P> LoadableField<T,P> defaultField(String key, T defaultValue, Function<P,T> getter) {
    return defaultField(key, defaultValue, false, getter);
  }


  /* Builders */

  /** Makes a list of this loadable */
  default Loadable<List<T>> list(int minSize) {
    return new ListLoadable<>(this, minSize);
  }

  /** Makes a list of this loadable */
  default Loadable<List<T>> list() {
    return list(1);
  }

  /** Makes a set of this loadable */
  default Loadable<Set<T>> set(int minSize) {
    return new SetLoadable<>(this, minSize);
  }

  /** Makes a set of this loadable */
  default Loadable<Set<T>> set() {
    return set(1);
  }


  /* Mapping */

  /** Flattens the given mapping function */
  private static <T,R> BiFunction<T,ErrorFactory,R> flatten(Function<T,R> function) {
    return (value, error) -> function.apply(value);
  }

  /** Maps this loader to another type, with error factory on both from and to */
  default <M> Loadable<M> map(BiFunction<T,ErrorFactory,M> from, BiFunction<M,ErrorFactory,T> to) {
    return new MappedLoadable<>(this, from, to);
  }

  /** Maps this loader to another type, with error factory on from */
  default <M> Loadable<M> comapFlatMap(BiFunction<T,ErrorFactory,M> from, Function<M,T> to) {
    return map(from, flatten(to));
  }

  /** Maps this loader to another type */
  default <M> Loadable<M> flatComap(Function<T,M> from, BiFunction<M,ErrorFactory,T> to) {
    return map(flatten(from), to);
  }

  /** Maps this loader to another type */
  default <M> Loadable<M> flatMap(Function<T,M> from, Function<M,T> to) {
    return map(flatten(from), flatten(to));
  }
}

package slimeknights.mantle.data.loadable;

import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.array.ObjectArrayLoadable;
import slimeknights.mantle.data.loadable.field.DefaultingField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.field.NullableField;
import slimeknights.mantle.data.loadable.field.RequiredField;
import slimeknights.mantle.data.loadable.field.TryDirectField;
import slimeknights.mantle.data.loadable.mapping.AnyCollectionLoadable;
import slimeknights.mantle.data.loadable.mapping.ListLoadable;
import slimeknights.mantle.data.loadable.mapping.MappedLoadable;
import slimeknights.mantle.data.loadable.mapping.SetLoadable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Interface for a generic loadable object. Handles serializing from and to JSON and decoding and encoding using binary byte buffers.
 * <p>
 * Acts as a sort of combination of {@link com.mojang.serialization.Codec} and {@link net.minecraft.network.codec.StreamCodec} though less efficient for types other than JSON.
 * @see slimeknights.mantle.data.codec.LoadableCodec
 * @see slimeknights.mantle.network.codec.StreamableCodec
 */
@SuppressWarnings("unused")  // API
public interface Loadable<T> extends SimpleJsonSerializer<T>, SimpleJsonDeserializer<T>, Streamable<T> {
  /* Fields */

  /** Creates a required field from this loadable */
  default <P> LoadableField<T,P> requiredField(String key, Function<P,T> getter) {
    return new RequiredField<>(this, key, false, getter);
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

  /** Creates a field that loads this object directly into the parent JSON object if possible (it serialized to JSON), otherwise loads it into a field */
  default <P> LoadableField<T,P> tryDirectField(String key, Function<P,T> getter, String... conflicts) {
    return new TryDirectField<>(this, key, getter, conflicts);
  }


  /* Collections */

  /** Makes an array of this loadable */
  default ArrayLoadable<T[]> array(IntFunction<T[]> constructor, boolean allowNull, int minSize, int maxSize) {
    return new ObjectArrayLoadable<>(this, constructor, minSize, maxSize, allowNull);
  }

  /** Makes an array of this loadable */
  default ArrayLoadable<T[]> array(IntFunction<T[]> constructor, boolean allowNull, int minSize) {
    return array(constructor, allowNull, minSize, Integer.MAX_VALUE);
  }

  /** Makes a list of this loadable */
  default ArrayLoadable<List<T>> list(int minSize) {
    return new ListLoadable<>(this, minSize);
  }

  /** Makes a list of this loadable */
  default ArrayLoadable<List<T>> list() {
    return list(1);
  }

  /** Makes a set of this loadable */
  default ArrayLoadable<Set<T>> set(int minSize) {
    return new SetLoadable<>(this, minSize);
  }

  /** Makes a set of this loadable */
  default ArrayLoadable<Set<T>> set() {
    return set(1);
  }

  /** Makes a map from this loadable with this as values using the getter to determine map keys */
  default <K> Loadable<Map<K,T>> mapWithKeys(int minSize, Function<T,K> keyGetter) {
    return AnyCollectionLoadable.setBacked(this, minSize).mapWithKeys(keyGetter);
  }

  /** Makes a map from this loadable with this as keys using the getter to determine map values */
  default <V> Loadable<Map<T,V>> mapWithValues(int minSize, Function<T,V> valueGetter) {
    return AnyCollectionLoadable.setBacked(this, minSize).mapWithValues(valueGetter);
  }


  /* Mapping */

  /** Creates a copy of this loadable overriding the network to use the given streamable. */
  default Loadable<T> withNetwork(Streamable<T> network) {
    return CompositeLoadable.of(this, network);
  }

  /** Maps this loader to another type, with error factory on both from and to */
  default <M> Loadable<M> xmap(BiFunction<T,ErrorFactory,M> from, BiFunction<M,ErrorFactory,T> to) {
    return MappedLoadable.of(this, from, to);
  }

  /** Maps this loader to another type, with error factory on from */
  default <M> Loadable<M> comapFlatMap(BiFunction<T,ErrorFactory,M> from, Function<M,T> to) {
    return xmap(from, MappedLoadable.flatten(to));
  }

  /** Maps this loader to another type */
  default <M> Loadable<M> flatComap(Function<T,M> from, BiFunction<M,ErrorFactory,T> to) {
    return xmap(MappedLoadable.flatten(from), to);
  }

  /** Maps this loader to another type */
  default <M> Loadable<M> flatXmap(Function<T,M> from, Function<M,T> to) {
    return xmap(MappedLoadable.flatten(from), MappedLoadable.flatten(to));
  }

  /**
   * Validates the result of this map using the given function. This is equivelent to {@link #xmap(BiFunction, BiFunction)} with the same argument twice.
   * @param validator  Validator function, returns instance if valid.
   * @return Validated loadable
   */
  default Loadable<T> validate(BiFunction<T,ErrorFactory,T> validator) {
    return xmap(validator, validator);
  }
}

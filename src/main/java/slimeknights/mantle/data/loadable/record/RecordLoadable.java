package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function13;
import com.mojang.datafixers.util.Function14;
import com.mojang.datafixers.util.Function15;
import com.mojang.datafixers.util.Function16;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.ContextStreamable;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.DirectField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.mapping.CompactLoadable;
import slimeknights.mantle.data.loadable.mapping.MappedLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base interface for record type loadables, and the home of their factory methods.
 * Record loaders directly serialize into JSON objects, meaning they are compatible with {@link slimeknights.mantle.data.registry.GenericLoaderRegistry}.
 * @param <T>  Type being loaded
 */
@SuppressWarnings("unused")  // API
public interface RecordLoadable<T> extends Loadable<T>, IGenericLoader<T>, ContextStreamable<T> {
  /* Deserializing */

  /**
   * Deserializes the object from json.
   * @param json     JSON object
   * @param context  Additional parsing context, used notably by recipe serializers to store the ID and serializer.
   *                 Will be {@link TypedMap#EMPTY} in nested usages unless {@link DirectField} is used.
   * @return  Parsed loadable value
   * @throws com.google.gson.JsonSyntaxException  If unable to read from JSON
   */
  T deserialize(JsonObject json, TypedMap context);

  /** Contextless implementation of {@link #deserialize(JsonObject, TypedMap)} for {@link IGenericLoader}. */
  @Override
  default T deserialize(JsonObject json) {
    return deserialize(json, TypedMap.empty());
  }

  @Override
  default T convert(JsonElement element, String key) {
    return deserialize(GsonHelper.convertToJsonObject(element, key));
  }


  /* Serializing */

  @Override
  void serialize(T object, JsonObject json);

  @Override
  default JsonElement serialize(T object) {
    JsonObject json = new JsonObject();
    serialize(object, json);
    return json;
  }


  /* IGenericLoader methods */

  /** @deprecated use {@link #decode(FriendlyByteBuf)} */
  @Deprecated(forRemoval = true)
  @Override
  default T fromNetwork(FriendlyByteBuf buffer) {
    return decode(buffer);
  }

  /** @deprecated use {@link #encode(FriendlyByteBuf, Object)} */
  @Deprecated(forRemoval = true)
  @Override
  default void toNetwork(T object, FriendlyByteBuf buffer) {
    encode(buffer, object);
  }

  /* Fields */

  /** Creates a field that loads this object directly into the parent JSON object */
  default <P> LoadableField<T,P> directField(Function<P,T> getter) {
    return new DirectField<>(this, getter);
  }

  /** Allows parsing from Json primitives and serializes compactly if the condition is met */
  default RecordLoadable<T> compact(Loadable<T> compact, Predicate<T> condition) {
    return CompactLoadable.of(this, compact, condition);
  }


  /* Mapping - switches to the record version of the methods */

  @Override
  default <M> RecordLoadable<M> xmap(BiFunction<T,ErrorFactory,M> from, BiFunction<M,ErrorFactory,T> to) {
    return MappedLoadable.of(this, from, to);
  }

  @Override
  default <M> RecordLoadable<M> comapFlatMap(BiFunction<T,ErrorFactory,M> from, Function<M,T> to) {
    return xmap(from, MappedLoadable.flatten(to));
  }

  @Override
  default <M> RecordLoadable<M> flatComap(Function<T,M> from, BiFunction<M,ErrorFactory,T> to) {
    return xmap(MappedLoadable.flatten(from), to);
  }

  @Override
  default <M> RecordLoadable<M> flatXmap(Function<T,M> from, Function<M,T> to) {
    return xmap(MappedLoadable.flatten(from), MappedLoadable.flatten(to));
  }

  @Override
  default RecordLoadable<T> validate(BiFunction<T,ErrorFactory,T> validator) {
    return xmap(validator, validator);
  }


  /* Helpers to create the final loadable */

  /** Creates a loadable with 1 parameters */
  static <A,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    Function<A,R> constructor) {
    return new RecordLoadable1<>(
      fieldA,
      constructor
    );
  }

  /** Creates a loadable with 2 parameters */
  static <A,B,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    BiFunction<A,B,R> constructor) {
    return new RecordLoadable2<>(
      fieldA,
      fieldB,
      constructor
    );
  }

  /** Creates a loadable with 3 parameters */
  static <A,B,C,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    Function3<A,B,C,R> constructor) {
    return new RecordLoadable3<>(
      fieldA,
      fieldB,
      fieldC,
      constructor
    );
  }

  /** Creates a loadable with 4 parameters */
  static <A,B,C,D,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    Function4<A,B,C,D,R> constructor) {
    return new RecordLoadable4<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      constructor
    );
  }

  /** Creates a loadable with 5 parameters */
  static <A,B,C,D,E,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    Function5<A,B,C,D,E,R> constructor) {
    return new RecordLoadable5<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      constructor
    );
  }

  /** Creates a loadable with 6 parameters */
  static <A,B,C,D,E,F,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    Function6<A,B,C,D,E,F,R> constructor) {
    return new RecordLoadable6<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      constructor
    );
  }

  /** Creates a loadable with 7 parameters */
  static <A,B,C,D,E,F,G,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    Function7<A,B,C,D,E,F,G,R> constructor) {
    return new RecordLoadable7<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      constructor
    );
  }

  /** Creates a loadable with 8 parameters */
  static <A,B,C,D,E,F,G,H,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    Function8<A,B,C,D,E,F,G,H,R> constructor) {
    return new RecordLoadable8<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      constructor
    );
  }

  /** Creates a loadable with 9 parameters */
  static <A,B,C,D,E,F,G,H,I,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    RecordField<I,? super R> fieldI,
    Function9<A,B,C,D,E,F,G,H,I,R> constructor) {
    return new RecordLoadable9<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      fieldI,
      constructor
    );
  }

  /** Creates a loadable with 10 parameters */
  static <A,B,C,D,E,F,G,H,I,J,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    RecordField<I,? super R> fieldI,
    RecordField<J,? super R> fieldJ,
    Function10<A,B,C,D,E,F,G,H,I,J,R> constructor) {
    return new RecordLoadable10<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      fieldI,
      fieldJ,
      constructor
    );
  }

  /** Creates a loadable with 11 parameters */
  static <A,B,C,D,E,F,G,H,I,J,K,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    RecordField<I,? super R> fieldI,
    RecordField<J,? super R> fieldJ,
    RecordField<K,? super R> fieldK,
    Function11<A,B,C,D,E,F,G,H,I,J,K,R> constructor) {
    return new RecordLoadable11<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      fieldI,
      fieldJ,
      fieldK,
      constructor
    );
  }

  /** Creates a loadable with 12 parameters */
  static <A,B,C,D,E,F,G,H,I,J,K,L,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    RecordField<I,? super R> fieldI,
    RecordField<J,? super R> fieldJ,
    RecordField<K,? super R> fieldK,
    RecordField<L,? super R> fieldL,
    Function12<A,B,C,D,E,F,G,H,I,J,K,L,R> constructor) {
    return new RecordLoadable12<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      fieldI,
      fieldJ,
      fieldK,
      fieldL,
      constructor
    );
  }

  /** Creates a loadable with 13 parameters */
  static <A,B,C,D,E,F,G,H,I,J,K,L,M,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    RecordField<I,? super R> fieldI,
    RecordField<J,? super R> fieldJ,
    RecordField<K,? super R> fieldK,
    RecordField<L,? super R> fieldL,
    RecordField<M,? super R> fieldM,
    Function13<A,B,C,D,E,F,G,H,I,J,K,L,M,R> constructor) {
    return new RecordLoadable13<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      fieldI,
      fieldJ,
      fieldK,
      fieldL,
      fieldM,
      constructor
    );
  }

  /** Creates a loadable with 14 parameters */
  static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    RecordField<I,? super R> fieldI,
    RecordField<J,? super R> fieldJ,
    RecordField<K,? super R> fieldK,
    RecordField<L,? super R> fieldL,
    RecordField<M,? super R> fieldM,
    RecordField<N,? super R> fieldN,
    Function14<A,B,C,D,E,F,G,H,I,J,K,L,M,N,R> constructor) {
    return new RecordLoadable14<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      fieldI,
      fieldJ,
      fieldK,
      fieldL,
      fieldM,
      fieldN,
      constructor
    );
  }

  /** Creates a loadable with 15 parameters */
  static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    RecordField<I,? super R> fieldI,
    RecordField<J,? super R> fieldJ,
    RecordField<K,? super R> fieldK,
    RecordField<L,? super R> fieldL,
    RecordField<M,? super R> fieldM,
    RecordField<N,? super R> fieldN,
    RecordField<O,? super R> fieldO,
    Function15<A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,R> constructor) {
    return new RecordLoadable15<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      fieldI,
      fieldJ,
      fieldK,
      fieldL,
      fieldM,
      fieldN,
      fieldO,
      constructor
    );
  }

  /** Creates a loadable with 16 parameters */
  static <A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,R> RecordLoadable<R> create(
    RecordField<A,? super R> fieldA,
    RecordField<B,? super R> fieldB,
    RecordField<C,? super R> fieldC,
    RecordField<D,? super R> fieldD,
    RecordField<E,? super R> fieldE,
    RecordField<F,? super R> fieldF,
    RecordField<G,? super R> fieldG,
    RecordField<H,? super R> fieldH,
    RecordField<I,? super R> fieldI,
    RecordField<J,? super R> fieldJ,
    RecordField<K,? super R> fieldK,
    RecordField<L,? super R> fieldL,
    RecordField<M,? super R> fieldM,
    RecordField<N,? super R> fieldN,
    RecordField<O,? super R> fieldO,
    RecordField<P,? super R> fieldP,
    Function16<A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,R> constructor) {
    return new RecordLoadable16<>(
      fieldA,
      fieldB,
      fieldC,
      fieldD,
      fieldE,
      fieldF,
      fieldG,
      fieldH,
      fieldI,
      fieldJ,
      fieldK,
      fieldL,
      fieldM,
      fieldN,
      fieldO,
      fieldP,
      constructor
    );
  }
}

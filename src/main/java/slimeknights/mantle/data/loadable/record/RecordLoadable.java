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
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.DirectField;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Base interface for record type loadables, and the home of their factory methods.
 * Record loaders directly serialize into JSON objects, meaning they are compatible with {@link slimeknights.mantle.data.registry.GenericLoaderRegistry}.
 * @param <T>  Type being loaded
 */
public interface RecordLoadable<T> extends Loadable<T>, IGenericLoader<T> {
  @Override
  T deserialize(JsonObject json);

  @Override
  default T convert(JsonElement element, String key) {
    return deserialize(GsonHelper.convertToJsonObject(element, key));
  }

  @Override
  void serialize(T object, JsonObject json);

  @Override
  default JsonElement serialize(T object) {
    JsonObject json = new JsonObject();
    serialize(object, json);
    return json;
  }

  /** Creates a field that loads this object directly into the parent JSON object */
  default <P> DirectField<T,P> directField(Function<P,T> getter) {
    return new DirectField<>(this, getter);
  }


  /* Helpers to create the final loadable */

  /** Creates a loadable with 1 parameters */
  static <A,R> RecordLoadable<R> create(
    LoadableField<A,R> fieldA,
    Function<A,R> constructor) {
    return new RecordLoadable1<>(
      fieldA,
      constructor
    );
  }

  /** Creates a loadable with 2 parameters */
  static <A,B,R> RecordLoadable<R> create(
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    BiFunction<A,B,R> constructor) {
    return new RecordLoadable2<>(
      fieldA,
      fieldB,
      constructor
    );
  }

  /** Creates a loadable with 3 parameters */
  static <A,B,C,R> RecordLoadable<R> create(
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
    LoadableField<I,R> fieldI,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
    LoadableField<I,R> fieldI,
    LoadableField<J,R> fieldJ,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
    LoadableField<I,R> fieldI,
    LoadableField<J,R> fieldJ,
    LoadableField<K,R> fieldK,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
    LoadableField<I,R> fieldI,
    LoadableField<J,R> fieldJ,
    LoadableField<K,R> fieldK,
    LoadableField<L,R> fieldL,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
    LoadableField<I,R> fieldI,
    LoadableField<J,R> fieldJ,
    LoadableField<K,R> fieldK,
    LoadableField<L,R> fieldL,
    LoadableField<M,R> fieldM,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
    LoadableField<I,R> fieldI,
    LoadableField<J,R> fieldJ,
    LoadableField<K,R> fieldK,
    LoadableField<L,R> fieldL,
    LoadableField<M,R> fieldM,
    LoadableField<N,R> fieldN,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
    LoadableField<I,R> fieldI,
    LoadableField<J,R> fieldJ,
    LoadableField<K,R> fieldK,
    LoadableField<L,R> fieldL,
    LoadableField<M,R> fieldM,
    LoadableField<N,R> fieldN,
    LoadableField<O,R> fieldO,
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
    LoadableField<A,R> fieldA,
    LoadableField<B,R> fieldB,
    LoadableField<C,R> fieldC,
    LoadableField<D,R> fieldD,
    LoadableField<E,R> fieldE,
    LoadableField<F,R> fieldF,
    LoadableField<G,R> fieldG,
    LoadableField<H,R> fieldH,
    LoadableField<I,R> fieldI,
    LoadableField<J,R> fieldJ,
    LoadableField<K,R> fieldK,
    LoadableField<L,R> fieldL,
    LoadableField<M,R> fieldM,
    LoadableField<N,R> fieldN,
    LoadableField<O,R> fieldO,
    LoadableField<P,R> fieldP,
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

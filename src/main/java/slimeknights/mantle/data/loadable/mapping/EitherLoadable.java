package slimeknights.mantle.data.loadable.mapping;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ContextStreamable;
import slimeknights.mantle.data.loadable.IAmLoadable;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/** Record loadable that chooses a loadable based on the presence of a key. */
public class EitherLoadable {
  private EitherLoadable() {}

  /** Option in the record loadable */
  private record KeyOption<T>(String key, RecordLoadable<? extends T> loadable) {}

  /** Creates a new builder for a multi-type loadable */
  public static <T extends IAmLoadable> TypedBuilder<T> typed() {
    return new TypedBuilder<>();
  }

  /** Creates a new builder for a record */
  public static <T extends IAmLoadable.Record> RecordBuilder<T> record() {
    return new RecordBuilder<>();
  }

  /** Builder class */
  public static class TypedBuilder<T extends IAmLoadable> {
    /** List loadable, if set allows parsing from arrays */
    private Loadable<? extends T> array = null;
    /** Primitive loadable, if set, allows parsing from primitives */
    private Loadable<? extends T> primitive = null;
    /** Object options to choose from by present key */
    protected final ImmutableList.Builder<KeyOption<T>> keys = ImmutableList.builder();

    private TypedBuilder() {}

    /** Adds a key option to the builder */
    public TypedBuilder<T> key(String key, RecordLoadable<? extends T> loadable) {
      keys.add(new KeyOption<>(key, loadable));
      return this;
    }

    /** Adds a list option to the builder, disallows building a record loaable at the end*/
    public TypedBuilder<T> array(Loadable<? extends T> loadable) {
      if (this.array != null) {
        throw new IllegalStateException("Duplicate array loadable, previous value " + this.array);
      }
      this.array = loadable;
      return this;
    }

    /** Adds a primitive option to the builder, disallows building a record loadable at the end */
    public TypedBuilder<T> primitive(Loadable<? extends T> loadable) {
      if (this.primitive != null) {
        throw new IllegalStateException("Duplicate primitive loadable, previous value " + this.primitive);
      }
      this.primitive = loadable;
      return this;
    }

    /** Gets the keys for the builder */
    private List<KeyOption<T>> getKeys() {
      List<KeyOption<T>> keys = this.keys.build();
      int size = keys.size() + (array != null ? 1 : 0) + (primitive != null ? 1 : 0);
      if (size < 2) {
        throw new IllegalStateException("EitherLoadable must have at least 2 options.");
      }
      return keys;
    }

    /** Builds the loadable with custom network logic */
    public Loadable<T> build(Streamable<T> network) {
      return new Typing<>(List.of(network), getKeys(), array, primitive);
    }

    /** Builds the loadable with the default network logic */
    @SuppressWarnings("unchecked")
    public Loadable<T> build() {
      List<KeyOption<T>> keys = getKeys();
      ImmutableList.Builder<Streamable<T>> builder = ImmutableList.builder();
      keys.forEach(key -> builder.add((Loadable<T>)key.loadable));
      if (array != null) {
        builder.add((Loadable<T>)array);
      }
      if (primitive != null) {
        builder.add((Loadable<T>)primitive);
      }
      return new Typing<>(builder.build(), getKeys(), array, primitive);
    }
  }

  /** Builder class */
  public static class RecordBuilder<T extends IAmLoadable.Record> {
    /** Object options to choose from by present key */
    protected final ImmutableList.Builder<KeyOption<T>> keys = ImmutableList.builder();

    private RecordBuilder() {}

    /** Adds a key option to the builder */
    public RecordBuilder<T> key(String key, RecordLoadable<? extends T> loadable) {
      keys.add(new KeyOption<>(key, loadable));
      return this;
    }

    /** Gets the keys for the builder */
    private List<KeyOption<T>> getKeys() {
      List<KeyOption<T>> keys = this.keys.build();
      if (keys.size() < 2) {
        throw new IllegalStateException("EitherLoadable must have at least 2 options.");
      }
      return keys;
    }

    /** Builds the loadable with custom network logic */
    public RecordLoadable<T> build(ContextStreamable<T> network) {
      return new Record<>(List.of(network), getKeys());
    }

    /** Builds the loadable with default network logic */
    @SuppressWarnings("unchecked")  // its safe with how we use it
    public RecordLoadable<T> build() {
      List<KeyOption<T>> keys = getKeys();
      List<ContextStreamable<T>> network = keys.stream().<ContextStreamable<T>>map(option -> (RecordLoadable<T>)option.loadable).toList();
      return new Record<>(network, keys);
    }
  }


  /** Common logic between the two implementations */
  private interface EitherImpl<T extends IAmLoadable,L extends Streamable<T>> extends Loadable<T> {
    /* Fields */
    List<L> network();
    List<KeyOption<T>> keys();
    @Nullable
    default Loadable<? extends T> array() {
      return null;
    }
    @Nullable
    default Loadable<? extends T> primitive() {
      return null;
    }


    /* JSON */

    /** Deserializes from the given JSON object */
    default T deserializeObject(JsonElement element, TypedMap context, String key) {
      List<KeyOption<T>> keys = this.keys();
      if (element.isJsonObject()) {
        JsonObject json = element.getAsJsonObject();
        for (KeyOption<T> option : keys) {
          if (json.has(option.key)) {
            return option.loadable.deserialize(json, context);
          }
        }
      }
      StringBuilder builder = new StringBuilder();
      builder.append("JSON at ").append(key).append(" must be one of: ");
      if (array() != null) {
        builder.append("array, ");
      }
      if (primitive() != null) {
        builder.append("primitive, ");
      }
      builder.append("object with key from [")
             .append(keys.stream().map(KeyOption::key).collect(Collectors.joining(", ")))
             .append(']');
      throw new JsonSyntaxException(builder.toString());
    }

    /** Gets the loadable instance from the buffer */
    default L loadableFromNetwork(FriendlyByteBuf buffer) {
      List<L> networks = network();
      // size 1 means we have a fixed network logic, use that
      int size = networks.size();
      if (size == 1) {
        return networks.get(0);
      }
      // the integer should be guaranteed to be a valid loadable, but just in case give a better exception
      int networkIndex = buffer.readVarInt();
      if (networkIndex < size) {
        return networks.get(networkIndex);
      }
      throw new DecoderException("Unknown network index " + networkIndex + " for EitherLoadable with network size " + size + ", this should not be possible.");
    }

    @Override
    default void encode(FriendlyByteBuf buffer, T object) {
      List<L> networks = network();
      // size 1 means we have a fixed network logic, use that
      if (networks.size() == 1) {
        networks.get(0).encode(buffer, object);
      } else {
        // we need to be able to recover which loadable was used on deserialization, so use the index in our list
        Loadable<?> objectLoadable = object.loadable();
        for (int i = 0; i < networks.size(); i++) {
          L network = networks.get(i);
          // indexof would do deep comparison, but reference comparison is way more efficient here
          if (network == objectLoadable) {
            buffer.writeVarInt(i);
            network.encode(buffer, object);
            return;
          }
        }
        throw new IllegalArgumentException("Unable to serialize " + object + " to network as its loadable " + objectLoadable + " is not allows in the EitherLoadable");
      }
    }
  }

  /** Loadable supporting list and array */
  private record Typing<T extends IAmLoadable>(List<Streamable<T>> network, List<KeyOption<T>> keys, @Nullable Loadable<? extends T> array, @Nullable Loadable<? extends T> primitive) implements EitherImpl<T,Streamable<T>> {
    @Override
    public T convert(JsonElement element, String key) {
      if (array != null && element.isJsonArray()) {
        return array.convert(element, key);
      }
      if (primitive != null && element.isJsonPrimitive()) {
        return primitive.convert(element, key);
      }
      if (!keys.isEmpty()) {
        return deserializeObject(element, TypedMap.empty(), key);
      }
      // no keys mean both array and primitive are valid, so that is the error
      throw new JsonSyntaxException("JSON at " + key + " must be one of: array, primitive");
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonElement serialize(T object) {
      return ((Loadable<T>)object.loadable()).serialize(object);
    }

    @Override
    public T decode(FriendlyByteBuf buffer) {
      return loadableFromNetwork(buffer).decode(buffer);
    }
  }

  /** Loadable only supporting record */
  private record Record<T extends IAmLoadable.Record>(List<ContextStreamable<T>> network, List<KeyOption<T>> keys) implements RecordLoadable<T>, EitherImpl<T,ContextStreamable<T>> {
    @Override
    public T deserialize(JsonObject json, TypedMap context) {
      return deserializeObject(json, context, "[root]");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(T object, JsonObject json) {
      ((RecordLoadable<T>)object.loadable()).serialize(object, json);
    }

    @Override
    public T decode(FriendlyByteBuf buffer, TypedMap context) {
      return loadableFromNetwork(buffer).decode(buffer, context);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, T object) {
      EitherImpl.super.encode(buffer, object);
    }
  }
}

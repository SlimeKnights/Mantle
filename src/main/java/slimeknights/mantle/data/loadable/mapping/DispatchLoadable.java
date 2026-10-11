package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import slimeknights.mantle.data.loadable.CompositeLoadable;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.SimpleJsonDeserializer;
import slimeknights.mantle.data.loadable.SimpleJsonSerializer;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.data.loadable.field.CompositeLoadableField;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.record.RecordJsonDeserializer;
import slimeknights.mantle.data.loadable.record.RecordJsonSerializer;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Record loadable that chooses a loadable based on the presence of a key or the type of field. */
public class DispatchLoadable {
  private DispatchLoadable() {}

  /** Option in the record loadable */
  private record Option<T>(RecordJsonDeserializer<? extends T> loadable, List<String> keys) {
    /** Checks if this option can deserialize the given object */
    private boolean matches(JsonObject json) {
      if (keys.isEmpty()) {
        return true;
      }
      for (String key : keys) {
        if (json.has(key)) {
          return true;
        }
      }
      return false;
    }
  }

  /** Creates a new builder instance loadable */
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /** Builder class */
  public static class Builder<T> {
    /** Object options to choose from by present key */
    private final List<Option<T>> options = new ArrayList<>();
    /** List loadable, if set allows parsing from arrays */
    @Nullable
    private Loadable<? extends T> array = null;
    /** Primitive loadable, if set, allows parsing from primitives */
    @Nullable
    private Loadable<? extends T> primitive = null;
    /** Logic for network. Can be either a loadable or created inline */
    @Nullable
    private Streamable<T> network = null;

    private Builder() {}

    /**
     * Adds an option to the builder which is directly deserialized if any of the passed keys are present
     * If no keys are passed, acts as the fallback when all other options fail.
     */
    public Builder<T> direct(RecordLoadable<? extends T> loadable, String... keys) {
      options.add(new Option<>(loadable, List.of(keys)));
      return this;
    }

    /** Adds an option to the builder which is deserialized from the given key when present. */
    public Builder<T> nested(String key, Loadable<? extends T> loadable) {
      return direct(new SimpleRecordLoadable<>(loadable, key, null, false), key);
    }

    /** Adds a list option to the builder, deserialized when it's an array. */
    public Builder<T> array(Loadable<? extends T> loadable) {
      if (this.array != null) {
        throw new IllegalStateException("Duplicate array loadable, previous value " + this.array);
      }
      this.array = loadable;
      return this;
    }

    /** Adds a primitive option to the builder, deserialized when the element is primitive. */
    public Builder<T> primitive(Loadable<? extends T> loadable) {
      if (this.primitive != null) {
        throw new IllegalStateException("Duplicate primitive loadable, previous value " + this.primitive);
      }
      this.primitive = loadable;
      return this;
    }

    /** Sets the network serializer, required for each dispatch loadable. */
    public Builder<T> network(Streamable<T> streamable) {
      if (this.network != null) {
        throw new IllegalStateException("Duplicate network, previous value " + this.network);
      }
      this.network = streamable;
      return this;
    }

    /** Gets the keys for the builder */
    private List<Option<T>> getOptions() {
      List<Option<T>> keys = List.copyOf(this.options);
      int size = keys.size() + (array != null ? 1 : 0) + (primitive != null ? 1 : 0);
      if (size < 2) {
        throw new IllegalStateException("EitherLoadable must have at least 2 options.");
      }
      return keys;
    }

    /** Validates then fetches the network instance */
    private Streamable<T> getNetwork() {
      if (network == null) {
        throw new IllegalStateException("Network must be set");
      }
      return network;
    }

    /** Builds a simple deserializer from the given options which does not support record parsing. */
    public SimpleJsonDeserializer<T> deserializer() {
      return new Deserializer<>(getOptions(), array, primitive);
    }

    /** Builds a record deserializer from the given options. */
    public RecordJsonDeserializer<T> recordDeserializer() {
      if (options.isEmpty()) {
        throw new IllegalStateException("Must have at least one key option for creating a record deserializer.");
      }
      return new Deserializer<>(getOptions(), array, primitive);
    }

    /** Builds the loadable with custom network logic */
    public Loadable<T> build(SimpleJsonSerializer<T> serializer) {
      return CompositeLoadable.of(deserializer(), serializer, getNetwork());
    }

    /** Builds the loadable with custom network logic */
    public RecordLoadable<T> build(RecordJsonSerializer<T> serializer) {
      return CompositeLoadable.of(recordDeserializer(), serializer, getNetwork());
    }

    /** Builds the loadable with custom network logic */
    public <P> RecordField<T,P> field(RecordJsonSerializer<T> serializer, Function<P,T> getter) {
      return new CompositeLoadableField<>(recordDeserializer(), serializer, getNetwork(), getter);
    }
  }

  /** Deserializer logic, combining the options, array, and primitive parts */
  private record Deserializer<T>(List<Option<T>> options, @Nullable Loadable<? extends T> array, @Nullable Loadable<? extends T> primitive) implements RecordJsonDeserializer<T> {
    @Override
    public T deserialize(JsonObject json, TypedMap context) {
      return deserializeObject(json, context, "[root]");
    }

    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      Loadable<? extends T> array = array();
      if (array != null && element.isJsonArray()) {
        return array.convert(element, key, context);
      }
      Loadable<? extends T> primitive = primitive();
      if (primitive != null && element.isJsonPrimitive()) {
        return primitive.convert(element, key, context);
      }
      List<Option<T>> keys = options();
      if (!keys.isEmpty()) {
        return deserializeObject(element, context, key);
      }
      // no keys mean both array and primitive are valid, so that is the error
      throw new JsonSyntaxException("JSON at " + key + " must be one of: array, primitive");
    }

    /** Deserializes from the given JSON object */
    private T deserializeObject(JsonElement element, TypedMap context, String key) {
      List<Option<T>> options = this.options();
      if (element.isJsonObject()) {
        JsonObject json = element.getAsJsonObject();
        for (Option<T> option : options) {
          if (option.matches(json)) {
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
        .append(options.stream().flatMap(option -> option.keys.stream()).collect(Collectors.joining(", ")))
        .append(']');
      throw new JsonSyntaxException(builder.toString());
    }
  }
}

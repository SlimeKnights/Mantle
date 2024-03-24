package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.function.Predicate;

/**
 * Loadable which uses a compact form if the condition is met
 * @param <T>  Object type
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CompactLoadable<T> implements Loadable<T> {
  private final Loadable<T> loadable;
  private final Loadable<T> compact;
  private final Predicate<T> compactCondition;

  /**
   * Creates a new instance for a general loadable
   * @param loadable          Base loadable, used under most circumstances
   * @param compact           Compact form, will be used to deserialize primitives, and in serialization based on conditions
   * @param compactCondition  If true, uses the compact loadable
   * @param <T>  Object type
   */
  public static <T> Loadable<T> of(Loadable<T> loadable, Loadable<T> compact, Predicate<T> compactCondition) {
    return new CompactLoadable<>(loadable, compact, compactCondition);
  }

  /**
   * Creates a new instance for a record loadable
   * @param loadable          Base loadable, used under most circumstances
   * @param compact           Compact form, will be used to deserialize primitives, and in serialization based on conditions
   * @param compactCondition  If true, uses the compact loadable
   * @param <T>  Object type
   */
  public static <T> RecordLoadable<T> of(RecordLoadable<T> loadable, Loadable<T> compact, Predicate<T> compactCondition) {
    return new Record<>(loadable, compact, compactCondition);
  }

  @Override
  public T convert(JsonElement element, String key) {
    if (element.isJsonPrimitive()) {
      return compact.convert(element, key);
    }
    return loadable.convert(element, key);
  }

  @Override
  public JsonElement serialize(T object) {
    if (compactCondition.test(object)) {
      return compact.serialize(object);
    }
    return loadable.serialize(object);
  }


  /* Networking */

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    return loadable.fromNetwork(buffer);
  }

  @Override
  public void toNetwork(T object, FriendlyByteBuf buffer) {
    loadable.toNetwork(object, buffer);
  }

  /** Extension for records */
  private static class Record<T> extends CompactLoadable<T> implements RecordLoadable<T> {
    private final RecordLoadable<T> loadable;
    public Record(RecordLoadable<T> loadable, Loadable<T> compact, Predicate<T> compactCondition) {
      super(loadable, compact, compactCondition);
      this.loadable = loadable;
    }

    @Override
    public T deserialize(JsonObject json) {
      return loadable.deserialize(json);
    }

    @Override
    public void serialize(T object, JsonObject json) {
      loadable.serialize(object, json);
    }
  }
}

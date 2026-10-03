package slimeknights.mantle.data.loadable.record;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/**
 * Record loadable for an object with only a single implementation
 */
@RequiredArgsConstructor
public class SingletonLoader<T> implements RecordLoadable<T> {
  @Getter
  private final T instance;

  /** Helper for creating a loader using an anonymous class */
  public SingletonLoader(Function<RecordLoadable<T>,T> creator) {
    this.instance = creator.apply(this);
  }

  @Override
  public T deserialize(JsonObject json, TypedMap context) {
    return instance;
  }

  @Override
  public void serialize(T object, JsonObject json) {}

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return instance;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T value) {}

  /** Helper to create a singleton object as an anonymous class */
  public static <T> T singleton(Function<RecordLoadable<T>,T> instance) {
    return new SingletonLoader<>(instance).getInstance();
  }
}

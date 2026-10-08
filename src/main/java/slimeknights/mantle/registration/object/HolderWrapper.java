package slimeknights.mantle.registration.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Class that mirrors {@link net.neoforged.neoforge.registries.DeferredHolder} but wraps 1 or more holders instead of being a holder itself.
 * Main notable difference is intended constructors will take multiple objects. If you only plan to use a single object it's better to extend deferred holder.
 */
@Getter
@RequiredArgsConstructor
public abstract class HolderWrapper<R,T extends R> implements Supplier<T>, IdAwareObject {
  /** Resource key associated with this object */
  protected final ResourceKey<R> key;
  /** Supplier to the registry entry */
  protected final Holder<R> holder;

  /**
   * Creates a new item object from a holder returned by {@link net.minecraft.core.Registry#registerForHolder(Registry, ResourceLocation, Object)}.
   */
  public HolderWrapper(Holder<R> holder) {
    this.key = Objects.requireNonNull(holder.getKey());
    this.holder = holder;
  }

  /**
   * Creates a new item object using the given registry object. This variant can resolve its name before the registry object entry resolves
   * @param holder  Object base
   */
  public HolderWrapper(DeferredHolder<R,T> holder) {
    this.key = holder.getKey();
    this.holder = holder;
  }

  /**
   * Creates a new item object using the given registry object. This variant can resolve its name before the registry object entry resolves
   * @param wrapper  Object base
   */
  protected HolderWrapper(HolderWrapper<R,? extends T> wrapper) {
    this.key = wrapper.key;
    this.holder = wrapper.holder;
  }

  @Override
  public ResourceLocation id() {
    return key.location();
  }
  /**
   * Gets the entry, throwing an exception if not valid
   * @return  Entry
   * @throws IllegalStateException  if not present
   * @throws ClassCastException     if the class of the object is different from the class in the holder.
   */
  @SuppressWarnings("unchecked")
  @Override
  public T get() {
    return (T) holder.value();
  }

  /**
   * Gets the entry, or null if its not present
   * @return  entry, or null if missing
   * @throws ClassCastException if the class of the object is different from the class in the holder.
   */
  @Nullable
  public T getOrNull() {
    if (holder.isBound()) {
      return get();
    }
    return null;
  }
}

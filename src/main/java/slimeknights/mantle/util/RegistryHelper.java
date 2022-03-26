package slimeknights.mantle.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RegistryHelper {
  private RegistryHelper() {}

  /** Gets the registry for the given key, dealing with tags */
  @Nullable
  @SuppressWarnings({"unchecked"})
  public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
    return (Registry<T>) Registry.REGISTRY.get(key.location());
  }

  /** Gets a stream of tag values for the given registry */
  public static <T> Stream<Holder<T>> getTagStream(Registry<T> registry, TagKey<T> key) {
    return StreamSupport.stream(registry.getTagOrEmpty(key).spliterator(), false);
  }

  /** Gets a stream of tag values for the given registry */
  public static <T> Stream<Holder<T>> getTagStream(TagKey<T> key) {
    Registry<T> registry = getRegistry(key.registry());
    if (registry == null) {
      return Stream.empty();
    }
    return getTagStream(registry, key);
  }
}

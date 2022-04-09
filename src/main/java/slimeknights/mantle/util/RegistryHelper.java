package slimeknights.mantle.util;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
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

  /** Gets a stream of tag holders for the given registry */
  public static <T> Stream<Holder<T>> getTagStream(Registry<T> registry, TagKey<T> key) {
    return StreamSupport.stream(registry.getTagOrEmpty(key).spliterator(), false);
  }

  /** Gets a stream of tag holders for the given registry */
  public static <T> Stream<Holder<T>> getTagStream(TagKey<T> key) {
    Registry<T> registry = getRegistry(key.registry());
    if (registry == null) {
      return Stream.empty();
    }
    return getTagStream(registry, key);
  }

  /** Gets a stream of tag values for the given registry */
  public static <T> Stream<T> getTagValueStream(Registry<T> registry, TagKey<T> key) {
    return getTagStream(registry, key).filter(Holder::isBound).map(Holder::value);
  }

  /** Gets a stream of tag values for the given registry */
  public static <T> Stream<T> getTagValueStream(TagKey<T> key) {
    return getTagStream(key).filter(Holder::isBound).map(Holder::value);
  }

  /** Checks if the given tag contains the given registry object */
  public static <T> boolean contains(Registry<T> registry, TagKey<T> tag, T value) {
    int index = registry.getId(value);
    if (index == Registry.DEFAULT) {
      return false;
    }
    return registry.getHolder(index).filter(holder -> holder.containsTag(tag)).isPresent();
  }

  /** Checks if the given tag contains the given registry object */
  public static <T> boolean contains(TagKey<T> tag, T value) {
    Registry<T> registry = getRegistry(tag.registry());
    if (registry == null) {
      return false;
    }
    return contains(registry, tag, value);
  }

  /** Checks if the given tag contains the given registry object */
  @SuppressWarnings("deprecation")
  public static boolean contains(TagKey<Block> tag, Block value) {
    return value.builtInRegistryHolder().is(tag);
  }

  /** Checks if the given tag contains the given registry object */
  @SuppressWarnings("deprecation")
  public static boolean contains(TagKey<Item> tag, Item value) {
    return value.builtInRegistryHolder().is(tag);
  }

  /** Checks if the given tag contains the given registry object */
  @SuppressWarnings("deprecation")
  public static boolean contains(TagKey<EntityType<?>> tag, EntityType<?> value) {
    return value.builtInRegistryHolder().is(tag);
  }

  /** Checks if the given tag contains the given registry object */
  @SuppressWarnings("deprecation")
  public static boolean contains(TagKey<Fluid> tag, Fluid value) {
    return value.builtInRegistryHolder().is(tag);
  }
}

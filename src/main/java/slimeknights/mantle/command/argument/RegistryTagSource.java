package slimeknights.mantle.command.argument;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/** Tags coming from a registry */
public record RegistryTagSource<T>(Registry<T> registry) implements TagSource<T> {
  @Override
  public ResourceKey<? extends Registry<T>> key() {
    return registry.key();
  }

  @Override
  public String folder() {
    return Registries.tagsDirPath(key());
  }

  /* Tags */

  @Override
  public boolean hasTag(TagKey<T> tag) {
    return registry.getTag(tag).isPresent();
  }

  @Override
  public Stream<TagKey<T>> tagKeys() {
    return registry.getTagNames();
  }


  /* Tag entries */

  @Nullable
  @Override
  public List<T> valuesInTag(TagKey<T> tag) {
    HolderSet.Named<T> holder = registry.getTag(tag).orElse(null);
    if (holder == null) {
      return null;
    }
    return holder.stream().filter(Holder::isBound).map(Holder::value).toList();
  }

  @Nullable
  @Override
  public List<ResourceLocation> keysInTag(TagKey<T> tag) {
    HolderSet.Named<T> holder = registry.getTag(tag).orElse(null);
    if (holder == null) {
      return null;
    }
    // I feel it should be way easier to get a resource location from a holder
    return holder.stream().filter(Holder::isBound).map(h -> registry.getKey(h.value())).toList();
  }


  /* Entries */

  @Nullable
  @Override
  public T getValue(ResourceLocation key) {
    // prevent defaulting registries from returning their default
    if (registry.containsKey(key)) {
      return registry.get(key);
    }
    return null;
  }

  @Override
  public Stream<TagKey<T>> tagsFor(T value) {
    return registry.getHolder(registry.getId(value)).stream().flatMap(Holder::tags);
  }

  @Override
  public Stream<ResourceLocation> valueKeys() {
    return registry.keySet().stream();
  }
}

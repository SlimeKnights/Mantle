package slimeknights.mantle.recipe.helper;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.util.LogicHelper;
import slimeknights.mantle.util.RegistryHelper;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility that helps get the preferred item from a tag based on mod ID.
 * @param <T>  Registry type
 */
public class TagPreference<T extends IForgeRegistryEntry<T>> {
  /** Just an alphabetically late RL to simplify null checks */
  private static final ResourceLocation DEFAULT_ID = new ResourceLocation("zzzzz:zzzzz"); // simplfies null checks
  /** Map of each tag type to the preference instance for that type */
  private static final Map<ResourceKey<?>, TagPreference<?>> PREFERENCE_MAP = new IdentityHashMap<>();

  /** Comparator to decide which registry entry is preferred */
  private static final Comparator<IForgeRegistryEntry<?>> ENTRY_COMPARATOR = (a, b) -> {
    // first get registry names, use default ID if null (unlikely)
    ResourceLocation idA = Objects.requireNonNullElse(a.getRegistryName(), DEFAULT_ID);
    ResourceLocation idB = Objects.requireNonNullElse(b.getRegistryName(), DEFAULT_ID);
    // first compare preferences
    List<? extends String> entries = Config.TAG_PREFERENCES.get();
    int size = entries.size();
    int indexA = LogicHelper.defaultIf(entries.indexOf(idA.getNamespace()), -1, size);
    int indexB = LogicHelper.defaultIf(entries.indexOf(idB.getNamespace()), -1, size);
    if (indexA != indexB) {
      return Integer.compare(indexA, indexB);
    }
    // for stability, fallback to registry name compare
    return idA.compareNamespaced(idB);
  };

  /**
   * Gets the tag preference instance associated with the given tag collection
   * @param key   Registry key for the relevant collection
   * @param <T>    Tag value type
   * @return  Tag preference instance
   */
  @SuppressWarnings("unchecked")
  public static <T extends IForgeRegistryEntry<T>> TagPreference<T> getInstance(ResourceKey<Registry<T>> key) {
    // should always be the right instance as only we add entries to the map
    return (TagPreference<T>) PREFERENCE_MAP.computeIfAbsent(key, c -> new TagPreference<>(key));
  }

  /**
   * Gets an instance for item tags
   * @return  Instance for item tags
   */
  public static TagPreference<Item> getItems() {
    return getInstance(Registry.ITEM_REGISTRY);
  }

  /**
   * Gets an instance for fluid tags
   * @return  Instance for fluid tags
   */
  public static TagPreference<Fluid> getFluids() {
    return getInstance(Registry.FLUID_REGISTRY);
  }

  /** Specific cache to this tag preference class type */
  private final Map<ResourceLocation, Optional<T>> preferenceCache = new HashMap<>();

  private TagPreference(ResourceKey<Registry<T>> key) {
    MinecraftForge.EVENT_BUS.addListener(this::clearCache);
  }

  /**
   * Clears the tag cache from the event
   * @param event  Tag event
   */
  private void clearCache(TagsUpdatedEvent event) {
    preferenceCache.clear();
  }



  /** Gets the preference from a tag without going through the cache, internal logic behind {@link #getPreference(TagKey)} */
  @SuppressWarnings("unchecked")
  private Optional<T> getUncachedPreference(TagKey<T> tag) {
    Registry<T> registry = (Registry<T>)Registry.REGISTRY.get(tag.registry().location());
    if (registry == null) {
      return Optional.empty();
    }
    // streams have a lovely function to get the minimum element based on a comparator
    // if the tag is empty, stream is empty so returns empty
    return RegistryHelper.getTagStream(tag).filter(Holder::isBound).map(Holder::value).min(ENTRY_COMPARATOR);
  }

  /**
   * Gets the preferred value from a tag based on mod ID
   * @param tag    Tag to fetch
   * @return  Preferred value from the tag, or empty optional if the tag is empty
   */
  public Optional<T> getPreference(TagKey<T> tag) {
    // fetch cached value if we have one
    return preferenceCache.computeIfAbsent(tag.location(), name -> getUncachedPreference(tag));
  }
}

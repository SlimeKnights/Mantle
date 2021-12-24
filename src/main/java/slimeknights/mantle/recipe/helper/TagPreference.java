package slimeknights.mantle.recipe.helper;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.util.LogicHelper;

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

  /** Supplier to tag collection */
  private final ResourceKey<Registry<T>> key;

  /** Specific cache to this tag preference class type */
  private final Map<ResourceLocation, Optional<T>> preferenceCache = new HashMap<>();

  private TagPreference(ResourceKey<Registry<T>> key) {
    this.key = key;
    MinecraftForge.EVENT_BUS.addListener(this::clearCache);
  }

  /**
   * Clears the tag cache from the event
   * @param event  Tag event
   */
  private void clearCache(TagsUpdatedEvent event) {
    preferenceCache.clear();
  }

  /** Gets the preference from a tag without going through the cache, internal logic behind {@link #getPreference(Tag)} */
  private Optional<T> getUncachedPreference(Tag<T> tag) {
    // if no items, empty optional
    if (tag instanceof IOptionalNamedTag && ((IOptionalNamedTag<?>) tag).isDefaulted()) {
      return Optional.empty();
    }
    List<? extends T> elements = tag.getValues();
    if (elements.isEmpty()) {
      return Optional.empty();
    }

    // if size 1, quick exit
    if (elements.size() == 1) {
      return Optional.of(elements.get(0));
    }
    // streams have a lovely function to get the minimum element based on a comparator
    return elements.stream()
                   .min(ENTRY_COMPARATOR)
                   .map(t -> (T) t); // required for generics to be happy
  }

  /**
   * Gets the preferred value from a tag based on mod ID
   * @param tag    Tag to fetch
   * @return  Preferred value from the tag, or empty optional if the tag is empty
   */
  public Optional<T> getPreference(Tag<T> tag) {
    // fetch cached value if we have one
    ResourceLocation tagName = SerializationTags.getInstance().getOrEmpty(key).getId(tag);
    if (tagName != null) {
      return preferenceCache.computeIfAbsent(tagName, name -> getUncachedPreference(tag));
    }
    Mantle.logger.warn("Attempting to get tag preference for unregistered tag {}", tag);
    return getUncachedPreference(tag);
  }
}

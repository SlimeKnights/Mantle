package slimeknights.mantle.recipe;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
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
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility that helps get the preferred item from a tag based on mod ID.
 * @param <T>  Registry type
 */
public class TagPreference<T extends IForgeRegistryEntry<T>> {
  /** Just an alphabetically late RL to simplify null checks */
  private static final ResourceLocation DEFAULT_ID = new ResourceLocation("zzzzz:zzzzz"); // simplfies null checks
  /** Map of each tag type to the preference instance for that type */
  private static final Map<Class<?>, TagPreference<?>> PREFERENCE_MAP = new IdentityHashMap<>();
  // cached tag supplier lambdas
  private static final Supplier<ITagCollection<Item>> ITEM_TAG_COLLECTION_SUPPLIER = () -> TagCollectionManager.getInstance().getItems();
  private static final Supplier<ITagCollection<Fluid>> FLUID_TAG_COLLECTION_SUPPLIER = () -> TagCollectionManager.getInstance().getFluids();

  /** Comparator to decide which registry entry is preferred */
  private static final Comparator<IForgeRegistryEntry<?>> ENTRY_COMPARATOR = (a, b) -> {
    // first get registry names, use default ID if null (unlikely)
    ResourceLocation idA = LogicHelper.defaultIfNull(a.getRegistryName(), DEFAULT_ID);
    ResourceLocation idB = LogicHelper.defaultIfNull(b.getRegistryName(), DEFAULT_ID);
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
   * @param clazz  Tag class
   * @param <T>    Tag value type
   * @return  Tag preference instance
   */
  @SuppressWarnings("unchecked")
  public static <T extends IForgeRegistryEntry<T>> TagPreference<T> getInstance(Class<T> clazz, Supplier<ITagCollection<T>> collection) {
    // should always be the right instance as only we add entries to the map
    return (TagPreference<T>) PREFERENCE_MAP.computeIfAbsent(clazz, c -> new TagPreference<>(collection));
  }

  /**
   * Gets an instance for item tags
   * @return  Instance for item tags
   */
  public static TagPreference<Item> getItems() {
    return getInstance(Item.class, ITEM_TAG_COLLECTION_SUPPLIER);
  }

  /**
   * Gets an instance for fluid tags
   * @return  Instance for fluid tags
   */
  public static TagPreference<Fluid> getFluids() {
    return getInstance(Fluid.class, FLUID_TAG_COLLECTION_SUPPLIER);
  }

  /** Supplier to tag collection */
  private final Supplier<ITagCollection<T>> collection;

  /** Specific cache to this tag preference class type */
  private final Map<ResourceLocation, Optional<T>> preferenceCache = new HashMap<>();

  private TagPreference(Supplier<ITagCollection<T>> collection) {
    this.collection = collection;
    MinecraftForge.EVENT_BUS.addListener(this::clearCache);
  }

  /**
   * Clears the tag cache from the event
   * @param event  Tag event
   */
  private void clearCache(TagsUpdatedEvent.VanillaTagTypes event) {
    preferenceCache.clear();
  }

  /** Gets the preference from a tag without going through the cache, internal logic behind {@link #getPreference(ITag)} */
  private Optional<T> getUncachedPreference(ITag<T> tag) {
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
  public Optional<T> getPreference(ITag<T> tag) {
    // fetch cached value if we have one
    try {
      ResourceLocation tagName = collection.get().getIdOrThrow(tag);
      return preferenceCache.computeIfAbsent(tagName, name -> getUncachedPreference(tag));
    } catch (Exception e) {
      Mantle.logger.warn("Attempting to get tag preference for unregistered tag {}", tag, e);
      return getUncachedPreference(tag);
    }
  }
}

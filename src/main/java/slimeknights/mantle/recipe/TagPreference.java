package slimeknights.mantle.recipe;

import com.google.common.collect.Lists;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import slimeknights.mantle.config.MantleConfig;

import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility that helps get the preferred item from a tag based on mod ID.
 * @param <T>  Registry type
 */
public class TagPreference<T extends IForgeRegistryEntry<T>> {
  /** Map of each tag type to the preference instance for that type */
  private static final Map<Class<?>, TagPreference<?>> PREFERENCE_MAP = new IdentityHashMap<>();
  // cached tag supplier lambdas
  private static final Supplier<TagGroup<Item>> ITEM_TAG_COLLECTION_SUPPLIER = () -> ServerTagManagerHolder.getTagManager().getItems();
  private static final Supplier<TagGroup<Fluid>> FLUID_TAG_COLLECTION_SUPPLIER = () -> ServerTagManagerHolder.getTagManager().getFluids();

  /**
   * Gets the tag preference instance associated with the given tag collection
   * @param clazz  Tag class
   * @param <T>    Tag value type
   * @return  Tag preference instance
   */
  @SuppressWarnings("unchecked")
  public static <T extends IForgeRegistryEntry<T>> TagPreference<T> getInstance(Class<T> clazz, Supplier<TagGroup<T>> collection) {
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
  private final Supplier<TagGroup<T>> collection;

  /** Specific cache to this tag preference class type */
  private final Map<Identifier, Optional<T>> preferenceCache = new HashMap<>();

  private TagPreference(Supplier<TagGroup<T>> collection) {
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

  /**
   * Gets the sort index of an entry based on the tag preference list
   * @param entry  Registry entry to check
   * @return  Sort index for that entry
   */
  private static int getSortIndex(IForgeRegistryEntry<?> entry) {
    // check the index of the namespace in the preference list
    int index = MantleConfig.TAG_PREFERENCES.indexOf(Objects.requireNonNull(entry.getRegistryName()).getNamespace());
    // if missing, declare last
    if (index == -1) {
      return MantleConfig.TAG_PREFERENCES.size();
    }
    return index;
  }

  /**
   * Gets the preferred value from a tag based on mod ID
   * @param tag    Tag to fetch
   * @return  Preferred value from the tag, or empty optional if the tag is empty
   */
  public Optional<T> getPreference(Tag<T> tag) {
    // fetch cached value if we have one
    Identifier tagName = collection.get().getTagId(tag);
    return preferenceCache.computeIfAbsent(tagName, name -> {
      // if no items, empty optional
      if (tag instanceof IOptionalNamedTag && ((IOptionalNamedTag<?>) tag).isDefaulted()) {
        return Optional.empty();
      }
      List<? extends T> elements = tag.values();
      if (elements.isEmpty()) {
        return Optional.empty();
      }

      // if size 1, quick exit
      if (elements.size() == 1) {
        return Optional.of(elements.get(0));
      }

      // copy and sort list
      List<? extends T> sortedElements = Lists.newArrayList(elements);
      sortedElements.sort(Comparator.comparingInt(TagPreference::getSortIndex));
      // return first element, its the preference
      return Optional.of(sortedElements.get(0));
    });
  }
}

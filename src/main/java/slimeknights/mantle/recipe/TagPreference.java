package slimeknights.mantle.recipe;

import com.google.common.collect.Lists;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import slimeknights.mantle.Mantle;

import java.util.*;
import java.util.function.Supplier;

/**
 * Utility that helps get the preferred item from a tag based on mod ID.
 * @param <T>  block/item tag
 */
public class TagPreference<T> {
  /** Map of each tag type to the preference instance for that type */
  private static final Map<Class<?>, TagPreference<?>> PREFERENCE_MAP = new IdentityHashMap<>();
  // cached tag supplier lambdas
  private static final Supplier<TagGroup<Item>> ITEM_TAG_COLLECTION_SUPPLIER = () -> ServerTagManagerHolder.getTagManager().getItems();
  private static final Supplier<TagGroup<Fluid>> FLUID_TAG_COLLECTION_SUPPLIER = () -> ServerTagManagerHolder.getTagManager().getFluids();
  private DefaultedRegistry<T> registry;


  /**
   * Gets an instance for item tags
   * @return  Instance for item tags
   */
  public static TagPreference<Item> getItems() {
    throw new RuntimeException("No known way of getting all item tags on fabric.");
//    return getInstance(Item.class, ITEM_TAG_COLLECTION_SUPPLIER);
  }

  /** Supplier to tag collection */
  private final Supplier<TagGroup<T>> collection;

  /** Specific cache to this tag preference class type */
  private final Map<Identifier, Optional<T>> preferenceCache = new HashMap<>();

  private TagPreference(Supplier<TagGroup<T>> collection) {
    this.collection = collection;
  }

  /**
   * Gets the sort index of an entry based on the tag preference list
   * @param entry  Registry entry to check
   * @return  Sort index for that entry
   */
  private int getSortIndex(T entry) {
    // check the index of the namespace in the preference list
    int index = Mantle.config.tagPreferences.indexOf(Objects.requireNonNull(registry.getId(entry)).getNamespace());
    // if missing, declare last
    if (index == -1) {
      return Mantle.config.tagPreferences.size();
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
      if (tag.values().size() == 0) {
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
      sortedElements.sort(Comparator.comparingInt(this::getSortIndex));
      // return first element, its the preference
      return Optional.of(sortedElements.get(0));
    });
  }
}

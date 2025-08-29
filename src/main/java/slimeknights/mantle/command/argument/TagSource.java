package slimeknights.mantle.command.argument;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/** Helper for sources of tag information. Allows creating a tag argument that is not bound to a registry. */
public interface TagSource<T> {
  /* Basic */

  /** Gets the key for this registry */
  ResourceKey<? extends Registry<T>> key();

  /** Gets the folder for this registry */
  String folder();


  /* Tags */

  /** Checks if the given tag is present */
  boolean hasTag(TagKey<T> tag);

  /** Checks if the given tag is present */
  default boolean hasTag(ResourceLocation tag) {
    return hasTag(TagKey.create(key(), tag));
  }

  /** Gets a stream of all tags in the registry */
  Stream<TagKey<T>> tagKeys();


  /* Tag values */

  /** Gets a collection of values in the given tag, or null if the tag is missing */
  @Nullable
  List<T> valuesInTag(TagKey<T> tag);

  /** Gets a stream of values in the given tag, or null if the tag is missing */
  @Nullable
  default List<T> valuesInTag(ResourceLocation tag) {
    return valuesInTag(TagKey.create(key(), tag));
  }

  /** Gets a collection of keys in the given tag, or null if the tag is missing */
  @Nullable
  List<ResourceLocation> keysInTag(TagKey<T> tag);

  /** Gets a stream of keys in the given tag, or null if the tag is missing */
  @Nullable
  default List<ResourceLocation> keysInTag(ResourceLocation tag) {
    return keysInTag(TagKey.create(key(), tag));
  }


  /* Values */

  /** Gets the value with the given key */
  @Nullable
  T getValue(ResourceLocation key);

  /** Gets all tag keys for the given value in the registry */
  Stream<TagKey<T>> tagsFor(T value);

  /** Gets a stream of all value keys in the registry */
  Stream<ResourceLocation> valueKeys();
}

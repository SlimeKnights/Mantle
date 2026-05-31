package slimeknights.mantle.recipe.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Condition checking for a combination of tags having any entries
 * @param match  List of tags that the entry must match
 * @param ignore Entries in this tag will be ignored towards the match. If null, all entries are considered
 * @param <T>  Registry type
 */
@SuppressWarnings("unused")
public record TagCombinationCondition<T>(List<TagKey<T>> match, @Nullable TagKey<T> ignore) implements ICondition {
  public static final ResourceLocation ID = Mantle.getResource("tag_combination_filled");
  private static final Codec<List<ResourceLocation>> MATCH_CODEC = Codec.either(ResourceLocation.CODEC, ResourceLocation.CODEC.listOf()).xmap(
    either -> either.map(List::of, locations -> locations),
    locations -> locations.size() == 1 ? Either.left(locations.get(0)) : Either.right(locations));
  public static final MapCodec<TagCombinationCondition<?>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    ResourceLocation.CODEC.optionalFieldOf("registry", Registries.ITEM.location()).forGetter(condition -> condition.match.get(0).registry().location()),
    MATCH_CODEC.fieldOf("match").forGetter(condition -> condition.match.stream().map(TagKey::location).toList()),
    ResourceLocation.CODEC.optionalFieldOf("ignore").forGetter(condition -> Optional.ofNullable(condition.ignore).map(TagKey::location))
  ).apply(instance, TagCombinationCondition::newCondition));

  private static TagCombinationCondition<?> newCondition(ResourceLocation registryName, List<ResourceLocation> match, Optional<ResourceLocation> ignore) {
    ResourceKey<Registry<Object>> registry = ResourceKey.createRegistryKey(registryName);
    return new TagCombinationCondition<>(
      match.stream().map(id -> TagKey.create(registry, id)).toList(),
      ignore.map(id -> TagKey.create(registry, id)).orElse(null));
  }

  public TagCombinationCondition {
    if (match.isEmpty()) {
      throw new IllegalArgumentException("Must match at least 1 tag");
    }
  }

  /** Creates a new instance ignoring the first tag and matching the rest */
  @SafeVarargs
  public static <T> TagCombinationCondition<T> match(@Nullable TagKey<T> ignore, TagKey<T>... match) {
    return new TagCombinationCondition<>(List.of(match), ignore);
  }

  /** Creates a new instance matching all the passed tags */
  @SafeVarargs
  public static <T> TagCombinationCondition<T> intersection(TagKey<T>... match) {
    return match(null, match);
  }

  /** Creates a new instance matching all the passed tags */
  public static <T> TagCombinationCondition<T> difference(TagKey<T> match, TagKey<T> ignore) {
    return match(ignore, match);
  }


  public ResourceLocation getID() {
    return ID;
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    // if there is just one tag, just needs to be filled
    List<Collection<Holder<T>>> tags = match.stream().map(context::getTag).toList();
    Collection<Holder<T>> ignored = ignore == null ? List.of() : context.getTag(ignore);
    if (tags.size() == 1 && ignored.isEmpty()) {
      return !tags.get(0).isEmpty();
    }
    // if any remaining tag is empty, give up
    int count = tags.size();
    for (int i = 1; i < count; i++) {
      if (tags.get(i).isEmpty()) {
        return false;
      }
    }

    // all tags have something, so find the first item that is in all tags
    itemLoop:
    for (Holder<T> entry : tags.get(0)) {
      if (ignored.contains(entry)) {
        continue;
      }
      // find the first item contained in all other intersection tags
      for (int i = 1; i < count; i++) {
        if (!tags.get(i).contains(entry)) {
          continue itemLoop;
        }
      }
      // all tags contain the item? success
      return true;
    }
    // no item in all tags
    return false;
  }

  public static final Serializer SERIALIZER = new Serializer();

  public static class Serializer {
    private static final Loadable<List<ResourceLocation>> MATCH = Loadables.RESOURCE_LOCATION.list(ArrayLoadable.COMPACT);

    public ResourceLocation getID() {
      return ID;
    }

    public void write(JsonObject json, TagCombinationCondition<?> value) {
      // save some space in JSON by not setting registry if item (most common)
      ResourceKey<?> registry = value.match.get(0).registry();
      if (!Registries.ITEM.equals(registry)) {
        json.addProperty("registry", registry.location().toString());
      }
      // serialize to a single field if just 1 name
      if (value.match.size() == 1) {
        json.addProperty("match", value.match.get(0).location().toString());
      } else {
        JsonArray names = new JsonArray();
        for (TagKey<?> name : value.match) {
          names.add(name.location().toString());
        }
        json.add("match", names);
      }
      if (value.ignore != null) {
        json.addProperty("ignore", value.ignore.location().toString());
      }
    }

    public TagCombinationCondition<?> read(JsonObject json) {
      // default to item registry if registry is unset
      ResourceKey<Registry<Object>> registry = ResourceKey.createRegistryKey(JsonHelper.getResourceLocation(json, "registry", Registries.ITEM.location()));
      return new TagCombinationCondition<>(
        MATCH.getIfPresent(json, "match").stream().map(id -> TagKey.create(registry, id)).toList(),
        json.has("ignore") ? TagKey.create(registry, JsonHelper.getResourceLocation(json, "ignore")) : null);
    }
  }
}

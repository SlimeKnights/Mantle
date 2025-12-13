package slimeknights.mantle.recipe.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Condition checking for a combination of tags having any entries
 * @param match  List of tags that the entry must match
 * @param ignore Entries in this tag will be ignored towards the match. If null, all entries are considered
 * @param <T>  Registry type
 */
@SuppressWarnings("unused")
public record TagCombinationCondition<T>(ResourceKey<? extends Registry<T>> registry, List<ResourceLocation> matchNames, @Nullable ResourceLocation ignoreName) implements ICondition {
  public static final ResourceLocation ID = Mantle.getResource("tag_combination_filled");
  
  private static final Codec<List<ResourceLocation>> MATCH_CODEC = Codec.either(ResourceLocation.CODEC, ResourceLocation.CODEC.listOf())
                                                                   .xmap(either -> either.map(List::of, Function.identity()),
                                                                         list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list));

  public static final MapCodec<TagCombinationCondition<?>> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    ResourceKey.codec(Registries.ROOT_REGISTRY_NAME).optionalFieldOf("registry", Registries.ITEM).forGetter(c -> c.registry),
    MATCH_CODEC.fieldOf("match").forGetter(TagCombinationCondition::matchNames),
    ResourceLocation.CODEC.optionalFieldOf("ignore").forGetter(c -> Optional.ofNullable(c.ignoreName))
  ).apply(inst, (reg, match, ignore) -> new TagCombinationCondition<>(reg, match, ignore.orElse(null))));

  public TagCombinationCondition {
    if (matchNames.isEmpty()) {
      throw new IllegalArgumentException("Must match at least 1 tag");
    }
  }

  /** Gets the match tags */
  public List<TagKey<T>> match() {
    return matchNames.stream().map(loc -> TagKey.create(registry, loc)).toList();
  }

  /** Gets the ignore tag */
  @Nullable
  public TagKey<T> ignore() {
    return ignoreName != null ? TagKey.create(registry, ignoreName) : null;
  }

  /** Creates a new instance ignoring the first tag and matching the rest */
  @SafeVarargs
  public static <T> TagCombinationCondition<T> match(@Nullable TagKey<T> ignore, TagKey<T>... match) {
    if (match.length == 0) throw new IllegalArgumentException("Must match at least 1 tag");
    ResourceKey<? extends Registry<T>> registry = match[0].registry();
    return new TagCombinationCondition<>(registry, 
      java.util.Arrays.stream(match).map(TagKey::location).toList(),
      ignore != null ? ignore.location() : null);
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

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    List<TagKey<T>> matchTags = match();
    TagKey<T> ignoreTag = ignore();
    
    // if there is just one tag, just needs to be filled
    List<Collection<Holder<T>>> tags = matchTags.stream().map(context::getTag).toList();
    Collection<Holder<T>> ignored = ignoreTag == null ? List.of() : context.getTag(ignoreTag);
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
}

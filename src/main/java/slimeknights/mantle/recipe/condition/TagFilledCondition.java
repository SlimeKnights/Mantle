package slimeknights.mantle.recipe.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.MantleLoot;

/** Inverted form of {@link TagEmptyCondition} as filled is way more common a desire than empty. */
public class TagFilledCondition<T> extends TagCondition<T> implements LootItemCondition {
  public static final Serializer<TagFilledCondition<?>> SERIALIZER = new Serializer<>(Mantle.getResource("tag_filled"), TagFilledCondition::new);
  public static final MapCodec<TagFilledCondition<?>> CODEC = TagCondition.codec(TagFilledCondition::new);

  public TagFilledCondition(TagKey<T> tag) {
    super(tag);
  }

  public TagFilledCondition(ResourceKey<? extends Registry<T>> registry, ResourceLocation name) {
    this(TagKey.create(registry, name));
  }

  public ResourceLocation getID() {
    return SERIALIZER.getID();
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.TAG_FILLED;
  }

  @Override
  public MapCodec<TagFilledCondition<?>> codec() {
    return CODEC;
  }

  @Override
  public boolean test(IContext context) {
    return !context.getTag(tag).isEmpty();
  }

  @Override
  public boolean test(LootContext context) {
    Registry<T> registry = registry(context);
    return registry != null && registry.getTagOrEmpty(tag).iterator().hasNext();
  }
}

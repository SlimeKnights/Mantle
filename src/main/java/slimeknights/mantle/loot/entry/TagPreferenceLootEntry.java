package slimeknights.mantle.loot.entry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.recipe.helper.TagPreference;

import java.util.List;
import java.util.function.Consumer;

/** Loot entry that returns the preferred item from a tag. See {@link TagPreference} */
public class TagPreferenceLootEntry extends LootPoolSingletonContainer {
  public static final MapCodec<TagPreferenceLootEntry> CODEC = RecordCodecBuilder.mapCodec(
    instance -> instance.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(entry -> entry.tag))
                        .and(singletonFields(instance))
                        .apply(instance, TagPreferenceLootEntry::new)
  );

  private final TagKey<Item> tag;
  protected TagPreferenceLootEntry(TagKey<Item> tag, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
    super(weight, quality, conditions, functions);
    this.tag = tag;
  }

  @Override
  public LootPoolEntryType getType() {
    return MantleLoot.TAG_PREFERENCE;
  }

  @Override
  protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
    TagPreference.getPreference(tag).ifPresent(item -> consumer.accept(new ItemStack(item)));
  }

  /** Creates a new builder */
  @SuppressWarnings("unused") // API
  public static Builder<?> tagPreference(TagKey<Item> tag) {
    return simpleBuilder((weight, quality, conditions, functions) -> new TagPreferenceLootEntry(tag, weight, quality, conditions, functions));
  }
}

package slimeknights.mantle.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.data.MantleCodecs;
import slimeknights.mantle.loot.MantleLoot;

import java.util.Optional;
import java.util.Set;

/** Variant of {@link net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition} that allows using a tag for block type instead of a block */
@SuppressWarnings("unused") // API
public record BlockTagLootCondition(TagKey<Block> tag, Optional<StatePropertiesPredicate> properties) implements LootItemCondition {
  public static final MapCodec<BlockTagLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MantleCodecs.BLOCK_TAG.fieldOf("tag").forGetter(BlockTagLootCondition::tag),
    StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(BlockTagLootCondition::properties)
  ).apply(instance, BlockTagLootCondition::new));

  public BlockTagLootCondition(TagKey<Block> tag) {
    this(tag, Optional.empty());
  }

  public BlockTagLootCondition(TagKey<Block> tag, StatePropertiesPredicate.Builder builder) {
    this(tag, builder.build());
  }

  @Override
  public boolean test(LootContext context) {
    BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
    return state != null && state.is(tag) && (this.properties.isEmpty() || this.properties.get().matches(state));
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return Set.of(LootContextParams.BLOCK_STATE);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.BLOCK_TAG_CONDITION.get();
  }
}

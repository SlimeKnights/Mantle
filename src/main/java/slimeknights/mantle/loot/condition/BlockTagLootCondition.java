package slimeknights.mantle.loot.condition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.util.JsonHelper;

import java.util.List;
import java.util.Set;

/** Variant of {@link net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition} that allows using a tag for block type instead of a block */
@RequiredArgsConstructor
public class BlockTagLootCondition implements LootItemCondition {
  private static final StatePropertiesPredicate ANY = new StatePropertiesPredicate(List.of());
  public static final MapCodec<BlockTagLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(condition -> condition.tag),
    StatePropertiesPredicate.CODEC.optionalFieldOf("properties", ANY).forGetter(condition -> condition.properties)
  ).apply(instance, BlockTagLootCondition::new));
  public static final SerializerImpl SERIALIZER = new SerializerImpl();

  private final TagKey<Block> tag;
  private final StatePropertiesPredicate properties;

  public BlockTagLootCondition(TagKey<Block> tag) {
    this(tag, ANY);
  }

  public BlockTagLootCondition(TagKey<Block> tag, StatePropertiesPredicate.Builder builder) {
    this(tag, builder.build().orElse(ANY));
  }

  @Override
  public boolean test(LootContext context) {
    BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
    return state != null && state.is(tag) && this.properties.matches(state);
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return Set.of(LootContextParams.BLOCK_STATE);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.BLOCK_TAG_CONDITION;
  }

  private static class SerializerImpl {
    public void serialize(JsonObject json, BlockTagLootCondition loot, JsonSerializationContext context) {
      json.addProperty("tag", loot.tag.location().toString());
      if (!loot.properties.properties().isEmpty()) {
        json.add("properties", StatePropertiesPredicate.CODEC.encodeStart(JsonOps.INSTANCE, loot.properties).getOrThrow(JsonSyntaxException::new));
      }
    }

    public BlockTagLootCondition deserialize(JsonObject json, JsonDeserializationContext context) {
      TagKey<Block> tag = TagKey.create(Registries.BLOCK, JsonHelper.getResourceLocation(json, "tag"));
      StatePropertiesPredicate predicate = ANY;
      if (json.has("properties")) {
        predicate = StatePropertiesPredicate.CODEC.parse(JsonOps.INSTANCE, json.get("properties")).getOrThrow(JsonSyntaxException::new);
      }
      return new BlockTagLootCondition(tag, predicate);
    }
  }
}

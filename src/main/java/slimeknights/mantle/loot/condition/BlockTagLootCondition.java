package slimeknights.mantle.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.util.JsonHelper;

import java.util.Objects;
import java.util.Set;

/** Variant of {@link net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition} that allows using a tag for block type instead of a block */
@RequiredArgsConstructor
public class BlockTagLootCondition implements LootItemCondition {
  public static final SerializerImpl SERIALIZER = new SerializerImpl();

  private final Tag<Block> tag;
  private final StatePropertiesPredicate properties;

  public BlockTagLootCondition(Tag<Block> tag) {
    this(tag, StatePropertiesPredicate.ANY);
  }

  public BlockTagLootCondition(Tag<Block> tag, StatePropertiesPredicate.Builder builder) {
    this(tag, builder.build());
  }

  @Override
  public boolean test(LootContext context) {
    BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
    return state != null && state.is(tag) && this.properties.matches(state);
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.BLOCK_STATE);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.BLOCK_TAG_CONDITION;
  }

  private static class SerializerImpl implements Serializer<BlockTagLootCondition> {
    @Override
    public void serialize(JsonObject json, BlockTagLootCondition loot, JsonSerializationContext context) {
      json.addProperty("tag", Objects.requireNonNull(SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY).getId(loot.tag)).toString());
      if (loot.properties != StatePropertiesPredicate.ANY) {
        json.add("properties", loot.properties.serializeToJson());
      }
    }

    @Override
    public BlockTagLootCondition deserialize(JsonObject json, JsonDeserializationContext context) {
      ResourceLocation id = JsonHelper.getResourceLocation(json, "tag");
      Tag<Block> tag = SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY).getTag(id);
      if (tag == null) {
        throw new JsonSyntaxException("Unknown block tag '" + id + "'");
      }
      StatePropertiesPredicate predicate = StatePropertiesPredicate.ANY;
      if (json.has("properties")) {
        predicate = StatePropertiesPredicate.fromJson(json.get("properties"));
      }
      return new BlockTagLootCondition(tag, predicate);
    }
  }
}

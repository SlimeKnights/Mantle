package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/** Loot modifier condition that evaluates a standard loot condition. Used to allow mixing both condition types in {@link AndLootModifierCondition} or {@link OrLootModifierCondition}. */
public record ContextLootModifierCondition(LootItemCondition condition) implements ILootModifierCondition {
  public static final MapCodec<ContextLootModifierCondition> CODEC = LootItemCondition.DIRECT_CODEC.fieldOf("condition").xmap(ContextLootModifierCondition::new, ContextLootModifierCondition::condition);

  @Override
  public MapCodec<ContextLootModifierCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    return condition.test(context);
  }
}

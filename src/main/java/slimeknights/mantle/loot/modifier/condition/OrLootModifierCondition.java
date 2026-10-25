package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

/** Condition matching when any of the nested conditions are true */
public record OrLootModifierCondition(List<ILootModifierCondition> conditions) implements ILootModifierCondition {
  public static final MapCodec<OrLootModifierCondition> CODEC = AndLootModifierCondition.LIST_CODEC.fieldOf("conditions").xmap(OrLootModifierCondition::new, OrLootModifierCondition::conditions);

  @Override
  public MapCodec<OrLootModifierCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    for (ILootModifierCondition condition : conditions) {
      if (condition.test(generatedLoot, context)) {
        return true;
      }
    }
    return false;
  }
}

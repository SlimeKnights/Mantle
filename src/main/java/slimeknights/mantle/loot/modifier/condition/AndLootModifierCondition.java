package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

/** Condition matching when all the nested conditions are true */
public record AndLootModifierCondition(List<ILootModifierCondition> conditions) implements ILootModifierCondition {
  static final Codec<List<ILootModifierCondition>> LIST_CODEC = LOADER.codec().listOf(2, Integer.MAX_VALUE);
  public static final MapCodec<AndLootModifierCondition> CODEC = LIST_CODEC.fieldOf("conditions").xmap(AndLootModifierCondition::new, AndLootModifierCondition::conditions);

  @Override
  public MapCodec<AndLootModifierCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    for (ILootModifierCondition condition : conditions) {
      if (!condition.test(generatedLoot, context)) {
        return false;
      }
    }
    return true;
  }
}

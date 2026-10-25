package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

/** Loot modifier condition that inverts the base condition */
public record InvertedLootModifierCondition(ILootModifierCondition condition) implements ILootModifierCondition {
  public static final MapCodec<InvertedLootModifierCondition> CODEC = ILootModifierCondition.LOADER.codec().fieldOf("condition").xmap(InvertedLootModifierCondition::new, InvertedLootModifierCondition::condition);

  @Override
  public MapCodec<InvertedLootModifierCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    return !condition.test(generatedLoot, context);
  }

  @Override
  public ILootModifierCondition inverted() {
    return condition;
  }
}

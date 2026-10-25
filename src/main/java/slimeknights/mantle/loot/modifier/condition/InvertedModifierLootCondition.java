package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

/** Loot modifier condition that inverts the base condition */
public record InvertedModifierLootCondition(ILootModifierCondition base) implements ILootModifierCondition {
  public static final MapCodec<InvertedModifierLootCondition> CODEC = ILootModifierCondition.LOADER.codec().fieldOf("base").xmap(InvertedModifierLootCondition::new, InvertedModifierLootCondition::base);

  @Override
  public MapCodec<InvertedModifierLootCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    return !base.test(generatedLoot, context);
  }

  @Override
  public ILootModifierCondition inverted() {
    return base;
  }
}

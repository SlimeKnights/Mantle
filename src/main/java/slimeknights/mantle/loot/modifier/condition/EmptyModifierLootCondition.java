package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

/** Loot condition to check if previously generated loot is empty */
public class EmptyModifierLootCondition implements ILootModifierCondition {
  public static final EmptyModifierLootCondition INSTANCE = new EmptyModifierLootCondition();
  public static final MapCodec<EmptyModifierLootCondition> CODEC = MapCodec.unit(INSTANCE);

  private EmptyModifierLootCondition() {}

  @Override
  public MapCodec<EmptyModifierLootCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    return generatedLoot.isEmpty();
  }
}

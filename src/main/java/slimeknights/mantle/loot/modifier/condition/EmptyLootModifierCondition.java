package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

/** Loot condition to check if previously generated loot is empty */
public class EmptyLootModifierCondition implements ILootModifierCondition {
  public static final EmptyLootModifierCondition INSTANCE = new EmptyLootModifierCondition();
  public static final MapCodec<EmptyLootModifierCondition> CODEC = MapCodec.unit(INSTANCE);

  private EmptyLootModifierCondition() {}

  @Override
  public MapCodec<EmptyLootModifierCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    return generatedLoot.isEmpty();
  }
}

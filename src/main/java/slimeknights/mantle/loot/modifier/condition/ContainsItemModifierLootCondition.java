package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.List;

/** Loot condition requiring one of the existing items is the given stack */
public record ContainsItemModifierLootCondition(Ingredient ingredient, int amountNeeded) implements ILootModifierCondition {
  public static final MapCodec<ContainsItemModifierLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ContainsItemModifierLootCondition::ingredient),
    ExtraCodecs.POSITIVE_INT.optionalFieldOf("needed", 1).forGetter(ContainsItemModifierLootCondition::amountNeeded)
  ).apply(instance, ContainsItemModifierLootCondition::new));

  public ContainsItemModifierLootCondition(Ingredient ingredient) {
    this(ingredient, 1);
  }

  @Override
  public MapCodec<ContainsItemModifierLootCondition> codec() {
    return CODEC;
  }

  @Override
  public boolean test(List<ItemStack> generatedLoot, LootContext context) {
    int matched = 0;
    for (ItemStack stack : generatedLoot) {
      if (ingredient.test(stack)) {
        matched += stack.getCount();
        if (matched >= amountNeeded) {
          return true;
        }
      }
    }
    return false;
  }
}

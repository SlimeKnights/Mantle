package slimeknights.mantle.loot.modifier.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.mantle.data.codec.SimpleCodecRegistry;

import java.util.List;

/** Condition for the global loot modifier add entry */
public interface ILootModifierCondition {
  /** Serializer to register conditions with */
  SimpleCodecRegistry<ILootModifierCondition> LOADER = new SimpleCodecRegistry<>("Loot Modifier Condition", ILootModifierCondition::codec);

  /** Checks if this condition passes */
  boolean test(List<ItemStack> generatedLoot, LootContext context);

  /** Gets the codec for serializing this condition to JSON */
  MapCodec<? extends ILootModifierCondition> codec();

  /** Creates an inverted instance of this condition */
  default ILootModifierCondition inverted() {
    return new InvertedModifierLootCondition(this);
  }
}

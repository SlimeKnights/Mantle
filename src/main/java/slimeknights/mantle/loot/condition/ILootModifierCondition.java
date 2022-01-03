package slimeknights.mantle.loot.condition;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.mantle.data.GenericRegisteredSerializer;
import slimeknights.mantle.data.GenericRegisteredSerializer.IJsonSerializable;

import java.util.List;

/** Condition for the global loot modifier add entry */
public interface ILootModifierCondition extends IJsonSerializable {
  /** Serializer to register conditions with */
  GenericRegisteredSerializer<ILootModifierCondition> MODIFIER_CONDITIONS = new GenericRegisteredSerializer<>();

  /** Checks if this condition passes */
  boolean test(List<ItemStack> generatedLoot, LootContext context);

  /** Creates an inverted instance of this condition */
  default ILootModifierCondition inverted() {
    return new InvertedModifierLootCondition(this);
  }
}

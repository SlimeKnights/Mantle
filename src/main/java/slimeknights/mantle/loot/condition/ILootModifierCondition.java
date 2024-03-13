package slimeknights.mantle.loot.condition;

import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.mantle.data.JsonCodec.GsonCodec;
import slimeknights.mantle.data.gson.GenericRegisteredSerializer;
import slimeknights.mantle.data.gson.GenericRegisteredSerializer.IJsonSerializable;

import java.util.List;

/** Condition for the global loot modifier add entry */
public interface ILootModifierCondition extends IJsonSerializable {
  /** Serializer to register conditions with */
  GenericRegisteredSerializer<ILootModifierCondition> MODIFIER_CONDITIONS = new GenericRegisteredSerializer<>();
  /** Codec instance for using this in GLMs, will probably migrate to a codec registry in the future since this is only needed for GLMs */
  Codec<ILootModifierCondition> CODEC = new GsonCodec<>("serializer", new GsonBuilder().registerTypeHierarchyAdapter(ILootModifierCondition.class, ILootModifierCondition.MODIFIER_CONDITIONS).create(), ILootModifierCondition.class);

  /** Checks if this condition passes */
  boolean test(List<ItemStack> generatedLoot, LootContext context);

  /** Creates an inverted instance of this condition */
  default ILootModifierCondition inverted() {
    return new InvertedModifierLootCondition(this);
  }
}

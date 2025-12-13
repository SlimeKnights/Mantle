package slimeknights.mantle.loot;

import com.google.gson.JsonDeserializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.loot.condition.EmptyModifierLootCondition;
import slimeknights.mantle.loot.condition.ILootModifierCondition;
import slimeknights.mantle.loot.condition.InvertedModifierLootCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.loot.function.RetexturedLootFunction;
import slimeknights.mantle.loot.function.SetFluidLootFunction;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;

import static slimeknights.mantle.loot.condition.ILootModifierCondition.MODIFIER_CONDITIONS;

public final class MantleLoot {
  private MantleLoot() {}

  private static boolean modifierConditionsRegistered = false;

  public static void init(IEventBus bus) {
    registerModifierConditions();
    GLOBAL_LOOT_MODIFIER_SERIALIZERS.register(bus);
    LOOT_CONDITION_TYPES.register(bus);
    LOOT_FUNCTION_TYPES.register(bus);
    LOOT_POOL_ENTRY_TYPES.register(bus);
  }

  // Deferred registers for NeoForge 1.21+
  public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLOBAL_LOOT_MODIFIER_SERIALIZERS =
      DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Mantle.modId);
  public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES =
      DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Mantle.modId);
  public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION_TYPES =
      DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Mantle.modId);
  public static final DeferredRegister<LootPoolEntryType> LOOT_POOL_ENTRY_TYPES =
      DeferredRegister.create(Registries.LOOT_POOL_ENTRY_TYPE, Mantle.modId);

  // Global loot modifiers
  public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddEntryLootModifier>> ADD_ENTRY =
      GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("add_entry", () -> AddEntryLootModifier.CODEC);
  public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ReplaceItemLootModifier>> REPLACE_ITEM =
      GLOBAL_LOOT_MODIFIER_SERIALIZERS.register("replace_item", () -> ReplaceItemLootModifier.CODEC);

  // Loot conditions
  /** Condition to match a block tag and property predicate */
  public static final DeferredHolder<LootItemConditionType, LootItemConditionType> BLOCK_TAG_CONDITION =
      LOOT_CONDITION_TYPES.register("block_tag", () -> new LootItemConditionType(BlockTagLootCondition.CODEC));
  /** Matches if the passed tag is empty */
  public static final DeferredHolder<LootItemConditionType, LootItemConditionType> TAG_EMPTY =
      LOOT_CONDITION_TYPES.register("tag_empty", () -> new LootItemConditionType(TagEmptyCondition.CODEC));
  /** Matches if the passed tag is filled */
  public static final DeferredHolder<LootItemConditionType, LootItemConditionType> TAG_FILLED =
      LOOT_CONDITION_TYPES.register("tag_filled", () -> new LootItemConditionType(TagFilledCondition.CODEC));

  // Loot functions
  /** Function to add block entity texture to a dropped item */
  public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<RetexturedLootFunction>> RETEXTURED_FUNCTION =
      LOOT_FUNCTION_TYPES.register("fill_retextured_block", () -> new LootItemFunctionType<>(RetexturedLootFunction.CODEC));
  /** Function to add a fluid to an item fluid capability */
  public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetFluidLootFunction>> SET_FLUID_FUNCTION =
      LOOT_FUNCTION_TYPES.register("set_fluid", () -> new LootItemFunctionType<>(SetFluidLootFunction.CODEC));

  // Loot entries
  /** Entry to pull a value from a tag preference */
  public static final DeferredHolder<LootPoolEntryType, LootPoolEntryType> TAG_PREFERENCE =
      LOOT_POOL_ENTRY_TYPES.register("tag_preference", () -> new LootPoolEntryType(TagPreferenceLootEntry.CODEC));

  /**
   * Called during mod initialization to register custom modifier conditions
   */
  public static void registerModifierConditions() {
    if (modifierConditionsRegistered) {
      return;
    }
    modifierConditionsRegistered = true;
    MODIFIER_CONDITIONS.registerDeserializer(InvertedModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)InvertedModifierLootCondition::deserialize);
    MODIFIER_CONDITIONS.registerDeserializer(EmptyModifierLootCondition.ID, EmptyModifierLootCondition.INSTANCE);
    MODIFIER_CONDITIONS.registerDeserializer(ContainsItemModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)ContainsItemModifierLootCondition::deserialize);
  }
}

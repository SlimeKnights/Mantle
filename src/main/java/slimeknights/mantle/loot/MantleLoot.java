package slimeknights.mantle.loot;

import com.google.gson.JsonDeserializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.loot.function.RetexturedLootFunction;
import slimeknights.mantle.loot.function.SetFluidLootFunction;
import slimeknights.mantle.loot.modifier.AddEntryLootModifier;
import slimeknights.mantle.loot.modifier.ReplaceItemLootModifier;
import slimeknights.mantle.loot.modifier.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.loot.modifier.condition.EmptyModifierLootCondition;
import slimeknights.mantle.loot.modifier.condition.ILootModifierCondition;
import slimeknights.mantle.loot.modifier.condition.InvertedModifierLootCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.registration.deferred.LootConditionDeferredRegister;
import slimeknights.mantle.registration.deferred.LootEntryDeferredRegister;
import slimeknights.mantle.registration.deferred.LootFunctionDeferredRegister;

import java.util.Objects;

import static slimeknights.mantle.loot.modifier.condition.ILootModifierCondition.MODIFIER_CONDITIONS;

/** Handles any loot table registration */
public class MantleLoot {
  private static final LootConditionDeferredRegister LOOT_CONDITIONS = new LootConditionDeferredRegister(Mantle.modId);
  private static final LootFunctionDeferredRegister LOOT_FUNCTIONS = new LootFunctionDeferredRegister(Mantle.modId);
  private static final LootEntryDeferredRegister LOOT_ENTRIES = new LootEntryDeferredRegister(Mantle.modId);


  private MantleLoot() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    LOOT_CONDITIONS.register(bus);
    LOOT_FUNCTIONS.register(bus);
    LOOT_ENTRIES.register(bus);
  }

  /** Matches if the passed tag is empty */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> TAG_EMPTY = LOOT_CONDITIONS.register("tag_empty", TagEmptyCondition.CODEC);
  /** Matches if the passed tag is filled */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> TAG_FILLED = LOOT_CONDITIONS.register("tag_filled", TagEmptyCondition.CODEC);
  /** Condition to match a block tag and property predicate */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> BLOCK_TAG_CONDITION = LOOT_CONDITIONS.register("block_tag", BlockTagLootCondition.CODEC);
  /** Condition for global loot modifiers that ensures a context set is present. Useful to check if we are in a specific context like entity. */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> HAS_CONTEXT_SET = LOOT_CONDITIONS.register("has_context_set", HasLootContextSetCondition.CODEC);

  /** Function to add block entity texture to a dropped item */
  public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<RetexturedLootFunction>> RETEXTURED_FUNCTION = LOOT_FUNCTIONS.register("fill_retextured_block", RetexturedLootFunction.CODEC);
  /** Function to add a fluid to an item fluid capability */
  public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetFluidLootFunction>> SET_FLUID_FUNCTION = LOOT_FUNCTIONS.register("set_fluid", SetFluidLootFunction.CODEC);

  /** Entry to pull a value from a tag preference */
  public static final DeferredHolder<LootPoolEntryType, LootPoolEntryType> TAG_PREFERENCE = LOOT_ENTRIES.register("tag_preference", TagPreferenceLootEntry.CODEC);

  /**
   * Called during serializer registration to register any relevant loot logic
   */
  public static void registerGlobalLootModifiers(final RegisterEvent event) {
    ResourceKey<?> key = event.getRegistryKey();
    if (key == NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS) {
      // global loot modifiers
      Registry<MapCodec<? extends IGlobalLootModifier>> registry = Objects.requireNonNull(event.getRegistry(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS));
      Registry.register(registry, Mantle.getResource("add_entry"), AddEntryLootModifier.CODEC);
      Registry.register(registry, Mantle.getResource("replace_item"), ReplaceItemLootModifier.CODEC);

      // loot modifier conditions
      MODIFIER_CONDITIONS.registerDeserializer(InvertedModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)InvertedModifierLootCondition::deserialize);
      MODIFIER_CONDITIONS.registerDeserializer(EmptyModifierLootCondition.ID, EmptyModifierLootCondition.INSTANCE);
      MODIFIER_CONDITIONS.registerDeserializer(ContainsItemModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)ContainsItemModifierLootCondition::deserialize);
    }
  }
}

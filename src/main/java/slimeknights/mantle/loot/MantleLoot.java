package slimeknights.mantle.loot;

import com.google.gson.JsonDeserializer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.loot.condition.EmptyModifierLootCondition;
import slimeknights.mantle.loot.condition.HasLootContextSetCondition;
import slimeknights.mantle.loot.condition.ILootModifierCondition;
import slimeknights.mantle.loot.condition.InvertedModifierLootCondition;
import slimeknights.mantle.loot.entry.TagPreferenceLootEntry;
import slimeknights.mantle.loot.function.RetexturedLootFunction;
import slimeknights.mantle.loot.function.SetFluidLootFunction;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.registration.adapter.RegistryAdapter;
import slimeknights.mantle.registration.deferred.LootConditionDeferredRegister;

import static slimeknights.mantle.loot.condition.ILootModifierCondition.MODIFIER_CONDITIONS;

/** Handles any loot table registration */
public class MantleLoot {
  private static final LootConditionDeferredRegister LOOT_CONDITIONS = new LootConditionDeferredRegister(Mantle.modId);

  /** Function to add block entity texture to a dropped item */
  public static LootItemFunctionType RETEXTURED_FUNCTION;
  /** Function to add a fluid to an item fluid capability */
  public static LootItemFunctionType SET_FLUID_FUNCTION;
  /** Entry to pull a value from a tag preference */
  public static LootPoolEntryType TAG_PREFERENCE;

  private MantleLoot() {}

  /** Registers this to the bus */
  public static void init(IEventBus bus) {
    LOOT_CONDITIONS.register(bus);
  }

  /** Matches if the passed tag is empty */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> TAG_EMPTY = LOOT_CONDITIONS.register("tag_empty", TagEmptyCondition.CODEC);
  /** Matches if the passed tag is filled */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> TAG_FILLED = LOOT_CONDITIONS.register("tag_filled", TagEmptyCondition.CODEC);
  /** Condition to match a block tag and property predicate */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> BLOCK_TAG_CONDITION = LOOT_CONDITIONS.register("block_tag", BlockTagLootCondition.CODEC);
  /** Condition for global loot modifiers that ensures a context set is present. Useful to check if we are in a specific context like entity. */
  public static final DeferredHolder<LootItemConditionType,LootItemConditionType> HAS_CONTEXT_SET = LOOT_CONDITIONS.register("has_context_set", HasLootContextSetCondition.CODEC);


  /**
   * Called during serializer registration to register any relevant loot logic
   */
  public static void registerGlobalLootModifiers(final RegisterEvent event) {
    ResourceKey<?> key = event.getRegistryKey();

    if (key == NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS) {
      RegistryAdapter<MapCodec<? extends IGlobalLootModifier>> adapter = new RegistryAdapter<>(event.getRegistry(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS));
      adapter.register(AddEntryLootModifier.CODEC, "add_entry");
      adapter.register(ReplaceItemLootModifier.CODEC, "replace_item");

      // loot modifier conditions
      MODIFIER_CONDITIONS.registerDeserializer(InvertedModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)InvertedModifierLootCondition::deserialize);
      MODIFIER_CONDITIONS.registerDeserializer(EmptyModifierLootCondition.ID, EmptyModifierLootCondition.INSTANCE);
      MODIFIER_CONDITIONS.registerDeserializer(ContainsItemModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)ContainsItemModifierLootCondition::deserialize);
    } else if (key == Registries.LOOT_FUNCTION_TYPE) {
      RETEXTURED_FUNCTION = registerFunction("fill_retextured_block", RetexturedLootFunction.SERIALIZER);
      SET_FLUID_FUNCTION = registerFunction("set_fluid", SetFluidLootFunction.SERIALIZER);

    } else if (key == Registries.LOOT_POOL_ENTRY_TYPE) {
      TAG_PREFERENCE = Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, Mantle.getResource("tag_preference"), new LootPoolEntryType(new TagPreferenceLootEntry.Serializer()));
    }
  }

  /**
   * Registers a loot function
   * @param name        Loot function name
   * @param serializer  Loot function serializer
   * @return  Registered loot function
   */
  private static LootItemFunctionType registerFunction(String name, Serializer<? extends LootItemFunction> serializer) {
    return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Mantle.getResource(name), new LootItemFunctionType(serializer));
  }
}

package slimeknights.mantle.loot;

import com.google.gson.JsonDeserializer;
import com.mojang.serialization.MapCodec;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
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
import slimeknights.mantle.recipe.condition.TagCombinationCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.registration.adapter.RegistryAdapter;

import java.util.Objects;

import static slimeknights.mantle.loot.condition.ILootModifierCondition.MODIFIER_CONDITIONS;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MantleLoot {
  /** Matches if the passed tag is empty */
  public static LootItemConditionType TAG_EMPTY;
  /** Matches if the passed tag is filled */
  public static LootItemConditionType TAG_FILLED;
  /** Condition to match a block tag and property predicate */
  public static LootItemConditionType BLOCK_TAG_CONDITION;
  /** Condition for global loot modifiers that ensures a context set is present. Useful to check if we are in a specific context like entity. */
  public static LootItemConditionType HAS_CONTEXT_SET;
  /** Function to add block entity texture to a dropped item */
  public static LootItemFunctionType<RetexturedLootFunction> RETEXTURED_FUNCTION;
  /** Function to add a fluid to an item fluid capability */
  public static LootItemFunctionType<SetFluidLootFunction> SET_FLUID_FUNCTION;
  /** Entry to pull a value from a tag preference */
  public static LootPoolEntryType TAG_PREFERENCE;


  /**
   * Called during serializer registration to register any relevant loot logic
   */
  public static void registerGlobalLootModifiers(final RegisterEvent event) {
    ResourceKey<?> key = event.getRegistryKey();

    if (key == NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS) {
      RegistryAdapter<MapCodec<? extends IGlobalLootModifier>> adapter = new RegistryAdapter<>(Objects.requireNonNull(event.getRegistry(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS)), Mantle.modId);
      adapter.register(AddEntryLootModifier.CODEC, "add_entry");
      adapter.register(ReplaceItemLootModifier.CODEC, "replace_item");

      // loot modifier conditions
      MODIFIER_CONDITIONS.registerDeserializer(InvertedModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)InvertedModifierLootCondition::deserialize);
      MODIFIER_CONDITIONS.registerDeserializer(EmptyModifierLootCondition.ID, EmptyModifierLootCondition.INSTANCE);
      MODIFIER_CONDITIONS.registerDeserializer(ContainsItemModifierLootCondition.ID, (JsonDeserializer<? extends ILootModifierCondition>)ContainsItemModifierLootCondition::deserialize);
    } else if (key == NeoForgeRegistries.Keys.CONDITION_CODECS) {
      RegistryAdapter<MapCodec<? extends ICondition>> adapter = new RegistryAdapter<>(Objects.requireNonNull(event.getRegistry(NeoForgeRegistries.Keys.CONDITION_CODECS)), Mantle.modId);
      adapter.register(TagCombinationCondition.CODEC, "tag_combination_filled");
      adapter.register(TagEmptyCondition.CODEC, "tag_empty");
      adapter.register(TagFilledCondition.CODEC, "tag_filled");
    } else if (key == Registries.LOOT_FUNCTION_TYPE) {
      RETEXTURED_FUNCTION = registerFunction("fill_retextured_block", RetexturedLootFunction.CODEC);
      SET_FLUID_FUNCTION = registerFunction("set_fluid", SetFluidLootFunction.CODEC);

    } else if (key == Registries.LOOT_CONDITION_TYPE) {
      BLOCK_TAG_CONDITION = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Mantle.getResource("block_tag"), new LootItemConditionType(BlockTagLootCondition.CODEC));
      HAS_CONTEXT_SET = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Mantle.getResource("has_context_set"), new LootItemConditionType(HasLootContextSetCondition.CODEC));
      TAG_EMPTY = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, TagEmptyCondition.SERIALIZER.getID(), new LootItemConditionType(TagEmptyCondition.CODEC));
      TAG_FILLED = Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, TagFilledCondition.SERIALIZER.getID(), new LootItemConditionType(TagFilledCondition.CODEC));

    } else if (key == Registries.LOOT_POOL_ENTRY_TYPE) {
      TAG_PREFERENCE = Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, Mantle.getResource("tag_preference"), new LootPoolEntryType(TagPreferenceLootEntry.CODEC));
    }
  }

  /**
   * Registers a loot function
   * @param name        Loot function name
   * @param codec       Loot function codec
   * @return  Registered loot function
   */
  private static <T extends LootItemFunction> LootItemFunctionType<T> registerFunction(String name, MapCodec<T> codec) {
    return Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Mantle.getResource(name), new LootItemFunctionType<>(codec));
  }
}

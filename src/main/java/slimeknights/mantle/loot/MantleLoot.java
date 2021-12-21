package slimeknights.mantle.loot;

import com.google.gson.JsonDeserializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.loot.condition.EmptyModifierLootCondition;
import slimeknights.mantle.loot.condition.ILootModifierCondition;
import slimeknights.mantle.loot.condition.InvertedModifierLootCondition;
import slimeknights.mantle.registration.adapter.RegistryAdapter;

import static slimeknights.mantle.registration.RegistrationHelper.injected;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ObjectHolder(value = Mantle.modId)
public class MantleLoot {
  static LootFunctionType RETEXTURED_FUNCTION;

  public static final AddEntryLootModifier.Serializer ADD_ENTRY = injected();
  public static final ReplaceItemLootModifier.Serializer REPLACE_ITEM = injected();

  /**
   * Called during serializer registration to register any relevant loot logic
   */
  public static void registerGlobalLootModifiers(final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
    RegistryAdapter<GlobalLootModifierSerializer<?>> adapter = new RegistryAdapter<>(event.getRegistry());
    adapter.register(new AddEntryLootModifier.Serializer(), "add_entry");
    adapter.register(new ReplaceItemLootModifier.Serializer(), "replace_item");

    // functions
    RETEXTURED_FUNCTION = registerFunction("fill_retextured_block", new RetexturedLootFunction.Serializer());

    // loot modifier conditions
    registerCondition(InvertedModifierLootCondition.ID, InvertedModifierLootCondition::deserialize);
    registerCondition(EmptyModifierLootCondition.ID, EmptyModifierLootCondition.INSTANCE);
    registerCondition(ContainsItemModifierLootCondition.ID, ContainsItemModifierLootCondition::deserialize);
  }

  /**
   * Registers a loot function
   * @param name        Loot function name
   * @param serializer  Loot function serializer
   * @return  Registered loot function
   */
  private static LootFunctionType registerFunction(String name, ILootSerializer<? extends ILootFunction> serializer) {
    return Registry.register(Registry.LOOT_FUNCTION_TYPE, Mantle.getResource(name), new LootFunctionType(serializer));
  }

  /**
   * Registers a loot condition
   * @param id            Loot condition id
   * @param deserializer  Loot condition deserializer
   */
  private static void registerCondition(ResourceLocation id, JsonDeserializer<? extends ILootModifierCondition> deserializer) {
    ILootModifierCondition.MODIFIER_CONDITIONS.registerDeserializer(id, deserializer);
  }
}

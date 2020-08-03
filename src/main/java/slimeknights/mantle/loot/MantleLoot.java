package slimeknights.mantle.loot;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.registry.Registry;
import slimeknights.mantle.Mantle;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MantleLoot {
  static LootFunctionType RETEXTURED_FUNCTION;

  /**
   * Called during serializer registration to register any relevant loot logic
   */
  public static void register() {
    RETEXTURED_FUNCTION = registerFunction("fill_retextured_block", new RetexturedLootFunction.Serializer());
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
}

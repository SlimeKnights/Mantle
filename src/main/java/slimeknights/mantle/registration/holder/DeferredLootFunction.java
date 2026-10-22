package slimeknights.mantle.registration.holder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;

/** {@link DeferredHolder} for a {@link LootItemFunctionType}. */
public class DeferredLootFunction<T extends LootItemFunction> extends DeferredHolder<LootItemFunctionType<?>,LootItemFunctionType<T>> {
  protected DeferredLootFunction(ResourceKey<LootItemFunctionType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends LootItemFunction> DeferredLootFunction<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(Registries.LOOT_FUNCTION_TYPE, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends LootItemFunction> DeferredLootFunction<T> createType(ResourceKey<LootItemFunctionType<?>> key) {
    return new DeferredLootFunction<>(key);
  }
}

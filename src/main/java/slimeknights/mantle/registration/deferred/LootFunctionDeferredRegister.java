package slimeknights.mantle.registration.deferred;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Deferred register extension for registering loot item condition types. */
public class LootFunctionDeferredRegister extends DeferredRegister<LootItemFunctionType<?>> {
  public LootFunctionDeferredRegister(String namespace) {
    super(Registries.LOOT_FUNCTION_TYPE, namespace);
  }

  /** Registers a loot condition from the given codec */
  public <T extends LootItemFunction> DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<T>> register(String name, MapCodec<T> codec) {
    return register(name, () -> new LootItemFunctionType<>(codec));
  }
}

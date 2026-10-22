package slimeknights.mantle.registration.deferred;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.holder.DeferredLootFunction;

import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for {@link LootItemFunctionType}, providing a non-supplier register method. */
public class LootFunctionDeferredRegister extends DeferredRegister<LootItemFunctionType<?>> {
  public LootFunctionDeferredRegister(String namespace) {
    super(Registries.LOOT_FUNCTION_TYPE, namespace);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends LootItemFunctionType<?>> DeferredHolder<LootItemFunctionType<?>, I> createHolder(ResourceKey<? extends Registry<LootItemFunctionType<?>>> registryKey, ResourceLocation key) {
    // this should be fine as LootItemFunctionType is a record
    return (DeferredHolder<LootItemFunctionType<?>, I>) DeferredLootFunction.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #register(String, MapCodec)} */
  @Deprecated
  @Override
  public <I extends LootItemFunctionType<?>> DeferredHolder<LootItemFunctionType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /** @deprecated use {@link #register(String, MapCodec)} */
  @Deprecated
  @Override
  public <I extends LootItemFunctionType<?>> DeferredHolder<LootItemFunctionType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** Registers a loot condition from the given codec */
  public <T extends LootItemFunction> DeferredLootFunction<T> register(String name, MapCodec<T> codec) {
    return (DeferredLootFunction<T>) register(name, () -> new LootItemFunctionType<>(codec));
  }
}

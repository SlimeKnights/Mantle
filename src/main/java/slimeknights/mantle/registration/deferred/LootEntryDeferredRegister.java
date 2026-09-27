package slimeknights.mantle.registration.deferred;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Deferred register extension for registering loot item condition types. */
public class LootEntryDeferredRegister extends DeferredRegister<LootPoolEntryType> {
  public LootEntryDeferredRegister(String namespace) {
    super(Registries.LOOT_POOL_ENTRY_TYPE, namespace);
  }

  /** Registers a loot condition from the given codec */
  public DeferredHolder<LootPoolEntryType, LootPoolEntryType> register(String name, MapCodec<? extends LootPoolEntryContainer> codec) {
    return register(name, () -> new LootPoolEntryType(codec));
  }
}

package slimeknights.mantle.registration.deferred;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Deferred register extension for registering loot item condition types. */
public class LootConditionDeferredRegister extends DeferredRegister<LootItemConditionType> {
  public LootConditionDeferredRegister(String namespace) {
    super(Registries.LOOT_CONDITION_TYPE, namespace);
  }

  /** Registers a loot condition from the given codec */
  public  DeferredHolder<LootItemConditionType, LootItemConditionType> register(String name, MapCodec<? extends LootItemCondition> codec) {
    return register(name, () -> new LootItemConditionType(codec));
  }
}

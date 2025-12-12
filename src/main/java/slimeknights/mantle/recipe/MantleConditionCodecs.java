package slimeknights.mantle.recipe;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.condition.TagCombinationCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;

/** Registers Mantle custom condition codecs for NeoForge 1.21+. */
public final class MantleConditionCodecs {
  private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS =
      DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Mantle.modId);

  private MantleConditionCodecs() {}

  public static void init(IEventBus bus) {
    CONDITION_CODECS.register(bus);
  }

  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagEmptyCondition<?>>> TAG_EMPTY =
      CONDITION_CODECS.register("tag_empty", () -> TagEmptyCondition.CODEC);

  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagFilledCondition<?>>> TAG_FILLED =
      CONDITION_CODECS.register("tag_filled", () -> TagFilledCondition.CODEC);

  public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<TagCombinationCondition<?>>> TAG_COMBINATION_FILLED =
      CONDITION_CODECS.register("tag_combination_filled", () -> TagCombinationCondition.CODEC);
}


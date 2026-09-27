package slimeknights.mantle.loot.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.loot.MantleLoot;

/** Loot condition that only runs if all required values in the given loot context set are present. Good heuristic for using that set. */
public record HasLootContextSetCondition(LootContextParamSet set) implements LootItemCondition {
  public static final MapCodec<HasLootContextSetCondition> CODEC = LootContextParamSets.CODEC.fieldOf("set").xmap(HasLootContextSetCondition::new, HasLootContextSetCondition::set);

  /** Creates a new builder instance */
  public static Builder builder(LootContextParamSet set) {
    return new Builder(set);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.HAS_CONTEXT_SET.get();
  }

  @Override
  public boolean test(LootContext context) {
    for (LootContextParam<?> param : set.getRequired()) {
      if (!context.hasParam(param)) {
        return false;
      }
    }
    return true;
  }

  /** Builder logic for this condition */
  public record Builder(LootContextParamSet set) implements LootItemCondition.Builder {
    /** @apiNote use {@link #builder(LootContextParamSet)} */
    @Internal
    public Builder {}

    @Override
    public LootItemCondition build() {
      return new HasLootContextSetCondition(set);
    }
  }
}

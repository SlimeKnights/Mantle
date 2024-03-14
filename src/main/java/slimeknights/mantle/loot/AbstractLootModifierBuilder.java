package slimeknights.mantle.loot;

import lombok.RequiredArgsConstructor;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Base builder for a global loot modifier during datagen, intended to be used with {@link GlobalLootModifierProvider} */
public abstract class AbstractLootModifierBuilder<B extends AbstractLootModifierBuilder<B>> {
  private final List<LootItemCondition> conditions = new ArrayList<>();

  /** Adds a condition to the builder */
  @SuppressWarnings("unchecked")
  public B addCondition(LootItemCondition condition) {
    conditions.add(condition);
    return (B) this;
  }

  /** Gets the built list of conditions */
  protected LootItemCondition[] getConditions() {
    return conditions.toArray(new LootItemCondition[0]);
  }

  /** Generic builder for a modifier that just takes conditions */
  @RequiredArgsConstructor
  public static class GenericLootModifierBuilder<M extends LootModifier> extends AbstractLootModifierBuilder<GenericLootModifierBuilder<M>> {
    private final Function<LootItemCondition[],M> constructor;

    /** Builds the final instance */
    public M build() {
      return constructor.apply(getConditions());
    }
  }
}

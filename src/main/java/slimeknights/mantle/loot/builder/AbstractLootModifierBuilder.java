package slimeknights.mantle.loot.builder;

import net.minecraft.loot.conditions.ILootCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

import java.util.ArrayList;
import java.util.List;

/** Base builder for a global loot modifier during datagen, intended to be used with {@link GlobalLootModifierProvider} */
public abstract class AbstractLootModifierBuilder<B extends AbstractLootModifierBuilder<B>> {
  private final List<ILootCondition> conditions = new ArrayList<>();

  /** Adds a condition to the builder */
  @SuppressWarnings("unchecked")
  public B addCondition(ILootCondition condition) {
    conditions.add(condition);
    return (B) this;
  }

  /** Gets the built list of conditions */
  protected ILootCondition[] getConditions() {
    return conditions.toArray(new ILootCondition[0]);
  }

  /** Builds the GLM */
  public abstract void build(String name, GlobalLootModifierProvider provider);
}

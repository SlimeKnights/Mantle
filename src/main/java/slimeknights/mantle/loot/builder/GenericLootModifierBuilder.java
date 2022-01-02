package slimeknights.mantle.loot.builder;

import lombok.RequiredArgsConstructor;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import java.util.function.Function;

/** Generic instantiation of the GLM builder */
@RequiredArgsConstructor(staticName = "builder")
public class GenericLootModifierBuilder<T extends LootModifier> extends AbstractLootModifierBuilder<GenericLootModifierBuilder<T>> {
  /** Serializer instance for this loot modifier */
  private final GlobalLootModifierSerializer<T> serializer;
  /** Constructor for the loot modifier */
  private final Function<ILootCondition[],T> constructor;

  @Override
  public void build(String name, GlobalLootModifierProvider provider) {
    provider.add(name, serializer, constructor.apply(getConditions()));
  }
}

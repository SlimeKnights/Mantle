package slimeknights.mantle.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.mantle.data.MantleCodecs;
import slimeknights.mantle.recipe.helper.ItemOutput;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;

/** Loot modifier to replace an item with another */
public class ReplaceItemLootModifier extends LootModifier {
  public static final Codec<ReplaceItemLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).and(
    inst.group(
      MantleCodecs.INGREDIENT.fieldOf("original").forGetter(m -> m.original),
      ItemOutput.CODEC.fieldOf("replacement").forGetter(m -> m.replacement),
      MantleCodecs.LOOT_FUNCTIONS.fieldOf("functions").forGetter(m -> m.functions)
    )).apply(inst, ReplaceItemLootModifier::new));

  /** Ingredient to test for the original item */
  private final Ingredient original;
  /** Item for the replacement */
  private final ItemOutput replacement;
  /** Functions to apply to the replacement */
  private final LootItemFunction[] functions;
  /** Functions merged into a single function for ease of use */
  private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

  protected ReplaceItemLootModifier(LootItemCondition[] conditionsIn, Ingredient original, ItemOutput replacement, LootItemFunction[] functions) {
    super(conditionsIn);
    this.original = original;
    this.replacement = replacement;
    this.functions = functions;
    this.combinedFunctions = LootItemFunctions.compose(functions);
  }

  /** Creates a builder to create a loot modifier */
  public static Builder builder(Ingredient original, ItemOutput replacement) {
    return new Builder(original, replacement);
  }

  @Nonnull
  @Override
  protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    ListIterator<ItemStack> iterator = generatedLoot.listIterator();
    while (iterator.hasNext()) {
      ItemStack stack = iterator.next();
      if (original.test(stack)) {
        ItemStack replacement = this.replacement.get();
        iterator.set(combinedFunctions.apply(ItemHandlerHelper.copyStackWithSize(replacement, replacement.getCount() * stack.getCount()), context));
      }
    }
    return generatedLoot;
  }

  @Override
  public Codec<? extends IGlobalLootModifier> codec() {
    return CODEC;
  }

  /** Logic to build this modifier for datagen */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends AbstractLootModifierBuilder<Builder> {
    private final Ingredient input;
    private final ItemOutput replacement;
    private final List<LootItemFunction> functions = new ArrayList<>();

    /**
     * Adds a loot function to the builder
     */
    public Builder addFunction(LootItemFunction function) {
      functions.add(function);
      return this;
    }

    /** Builds the final modifier */
    public ReplaceItemLootModifier build() {
      return new ReplaceItemLootModifier(getConditions(), input, replacement, functions.toArray(new LootItemFunction[0]));
    }
  }
}

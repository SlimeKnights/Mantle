package slimeknights.mantle.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.mantle.loot.builder.AbstractLootModifierBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** Loot modifier to replace an item with another */
public class ReplaceItemLootModifier extends LootModifier {
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
  protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
    return generatedLoot.stream().map(stack -> {
      if (original.test(stack)) {
        ItemStack replacement = this.replacement.get();
        return combinedFunctions.apply(ItemHandlerHelper.copyStackWithSize(replacement, replacement.getCount() * stack.getCount()), context);
      }
      return stack;
    }).collect(Collectors.toList());
  }

  public static class Serializer extends GlobalLootModifierSerializer<ReplaceItemLootModifier> {
    @Override
    public ReplaceItemLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
      Ingredient original;
      JsonElement element = JsonHelper.getElement(object, "original");
      if (element.isJsonPrimitive()) {
        original = Ingredient.of(RecipeHelper.deserializeItem(element.getAsString(), "original", Item.class));
      } else {
        original = Ingredient.fromJson(element);
      }
      ItemOutput replacement = ItemOutput.fromJson(JsonHelper.getElement(object, "replacement"));
      // functions
      LootItemFunction[] functions;
      if (object.has("functions")) {
        functions = AddEntryLootModifier.GSON.fromJson(GsonHelper.getAsJsonArray(object, "functions"), LootItemFunction[].class);
      } else {
        functions = new LootItemFunction[0];
      }
      return new ReplaceItemLootModifier(conditions, original, replacement, functions);
    }

    @Override
    public JsonObject write(ReplaceItemLootModifier instance) {
      JsonObject object = makeConditions(instance.conditions);
      object.add("original", instance.original.toJson());
      object.add("replacement", instance.replacement.serialize());
      if (instance.functions.length > 0) {
        object.add("functions", AddEntryLootModifier.GSON.toJsonTree(instance.functions, LootItemFunction[].class));
      }
      return object;
    }
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

    @Override
    public void build(String name, GlobalLootModifierProvider provider) {
      provider.add(name, MantleLoot.REPLACE_ITEM, new ReplaceItemLootModifier(getConditions(), input, replacement, functions.toArray(new LootItemFunction[0])));
    }
  }
}

package slimeknights.mantle.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
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
  private final ILootFunction[] functions;
  /** Functions merged into a single function for ease of use */
  private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

  protected ReplaceItemLootModifier(ILootCondition[] conditionsIn, Ingredient original, ItemOutput replacement, ILootFunction[] functions) {
    super(conditionsIn);
    this.original = original;
    this.replacement = replacement;
    this.functions = functions;
    this.combinedFunctions = LootFunctionManager.compose(functions);
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
    public ReplaceItemLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
      Ingredient original;
      JsonElement element = JsonHelper.getElement(object, "original");
      if (element.isJsonPrimitive()) {
        original = Ingredient.of(RecipeHelper.deserializeItem(element.getAsString(), "original", Item.class));
      } else {
        original = Ingredient.fromJson(element);
      }
      ItemOutput replacement = ItemOutput.fromJson(JsonHelper.getElement(object, "replacement"));
      // functions
      ILootFunction[] functions;
      if (object.has("functions")) {
        functions = AddEntryLootModifier.GSON.fromJson(JSONUtils.getAsJsonArray(object, "functions"), ILootFunction[].class);
      } else {
        functions = new ILootFunction[0];
      }
      return new ReplaceItemLootModifier(conditions, original, replacement, functions);
    }

    @Override
    public JsonObject write(ReplaceItemLootModifier instance) {
      JsonObject object = makeConditions(instance.conditions);
      object.add("original", instance.original.toJson());
      object.add("replacement", instance.replacement.serialize());
      if (instance.functions.length > 0) {
        object.add("functions", AddEntryLootModifier.GSON.toJsonTree(instance.functions, ILootFunction[].class));
      }
      return object;
    }
  }

  /** Logic to build this modifier for datagen */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends AbstractLootModifierBuilder<Builder> {
    private final Ingredient input;
    private final ItemOutput replacement;
    private final List<ILootFunction> functions = new ArrayList<>();

    /**
     * Adds a loot function to the builder
     */
    public Builder addFunction(ILootFunction function) {
      functions.add(function);
      return this;
    }

    @Override
    public void build(String name, GlobalLootModifierProvider provider) {
      provider.add(name, MantleLoot.REPLACE_ITEM, new ReplaceItemLootModifier(getConditions(), input, replacement, functions.toArray(new ILootFunction[0])));
    }
  }
}

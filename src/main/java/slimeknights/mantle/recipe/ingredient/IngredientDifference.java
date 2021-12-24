package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Ingredient that matches everything from another ingredient, minus a second ingredient
 */
public class IngredientDifference extends Ingredient {
  public static final ResourceLocation ID = Mantle.getResource("difference");
  public static final IIngredientSerializer<IngredientDifference> SERIALIZER = new Serializer();

  private final Ingredient base;
  private final Ingredient subtracted;
  private ItemStack[] filteredMatchingStacks;
  private IntList packedMatchingStacks;

  protected IngredientDifference(Ingredient base, Ingredient subtracted) {
    super(Stream.empty());
    this.base = base;
    this.subtracted = subtracted;
  }

  /**
   * Gets the set difference from the two ingredients
   * @param base     Base ingredient
   * @param subtracted  Ingredient to subtract
   * @return  Ingredient that {@code base} anything in base that is not in {@code without}
   */
  public static IngredientDifference without(Ingredient base, Ingredient subtracted) {
    return new IngredientDifference(base, subtracted);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return false;
    }
    return base.test(stack) && !subtracted.test(stack);
  }

  @Override
  public ItemStack[] getItems() {
    if (this.filteredMatchingStacks == null) {
      this.filteredMatchingStacks = Arrays.stream(base.getItems())
                                          .filter(stack -> !subtracted.test(stack))
                                          .toArray(ItemStack[]::new);
    }
    return filteredMatchingStacks;
  }

  @Override
  public boolean isEmpty() {
    return getItems().length == 0;
  }

  @Override
  public boolean isSimple() {
    return base.isSimple() && subtracted.isSimple();
  }

  @Override
  protected void invalidate() {
    super.invalidate();
    this.filteredMatchingStacks = null;
    this.packedMatchingStacks = null;
  }

  @Override
  public IntList getStackingIds() {
    if (this.packedMatchingStacks == null) {
      ItemStack[] matchingStacks = getItems();
      this.packedMatchingStacks = new IntArrayList(matchingStacks.length);
      for(ItemStack stack : matchingStacks) {
        this.packedMatchingStacks.add(RecipeItemHelper.getStackingIndex(stack));
      }
      this.packedMatchingStacks.sort(IntComparators.NATURAL_COMPARATOR);
    }
    return packedMatchingStacks;
  }

  @Override
  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", ID.toString());
    json.add("base", base.toJson());
    json.add("subtracted", subtracted.toJson());
    return json;
  }

  @Override
  public IIngredientSerializer<IngredientDifference> getSerializer() {
    return SERIALIZER;
  }

  private static class Serializer implements IIngredientSerializer<IngredientDifference> {
    @Override
    public IngredientDifference parse(JsonObject json) {
      Ingredient base = Ingredient.fromJson(JsonHelper.getElement(json, "base"));
      Ingredient without = Ingredient.fromJson(JsonHelper.getElement(json, "subtracted"));
      return new IngredientDifference(base, without);
    }

    @Override
    public IngredientDifference parse(PacketBuffer buffer) {
      Ingredient base = Ingredient.fromNetwork(buffer);
      Ingredient without = Ingredient.fromNetwork(buffer);
      return new IngredientDifference(base, without);
    }

    @Override
    public void write(PacketBuffer buffer, IngredientDifference ingredient) {
      CraftingHelper.write(buffer, ingredient.base);
      CraftingHelper.write(buffer, ingredient.subtracted);
    }
  }
}

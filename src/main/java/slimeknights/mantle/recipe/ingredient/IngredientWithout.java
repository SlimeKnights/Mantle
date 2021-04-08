package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Ingredient that matches everything from another ingredient, without a second
 */
public class IngredientWithout extends Ingredient {
  public static final Identifier ID = Mantle.getResource("without");
  public static final IIngredientSerializer<IngredientWithout> SERIALIZER = new Serializer();

  private final Ingredient base;
  private final Ingredient without;
  private ItemStack[] filteredMatchingStacks;
  private IntList packedMatchingStacks;
  public IngredientWithout(Ingredient base, Ingredient without) {
    super(Stream.empty());
    this.base = base;
    this.without = without;
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return false;
    }
    return base.test(stack) && !without.test(stack);
  }

  @Override
  public ItemStack[] getMatchingStacksClient() {
    if (this.filteredMatchingStacks == null) {
      this.filteredMatchingStacks = Arrays.stream(base.getMatchingStacksClient())
                                          .filter(stack -> !without.test(stack))
                                          .toArray(ItemStack[]::new);
    }
    return filteredMatchingStacks;
  }

  @Override
  public boolean isEmpty() {
    return getMatchingStacksClient().length == 0;
  }

  @Override
  public boolean isSimple() {
    return base.isSimple() && without.isSimple();
  }

  @Override
  protected void invalidate() {
    super.invalidate();
    this.filteredMatchingStacks = null;
    this.packedMatchingStacks = null;
  }

  @Override
  public IntList getIds() {
    if (this.packedMatchingStacks == null) {
      ItemStack[] matchingStacks = getMatchingStacksClient();
      this.packedMatchingStacks = new IntArrayList(matchingStacks.length);
      for(ItemStack stack : matchingStacks) {
        this.packedMatchingStacks.add(RecipeFinder.getItemId(stack));
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
    json.add("without", without.toJson());
    return json;
  }

  @Override
  public IIngredientSerializer<IngredientWithout> getSerializer() {
    return SERIALIZER;
  }

  private static class Serializer implements IIngredientSerializer<IngredientWithout> {
    @Override
    public IngredientWithout parse(JsonObject json) {
      Ingredient base = Ingredient.fromJson(JsonHelper.getElement(json, "base"));
      Ingredient without = Ingredient.fromJson(JsonHelper.getElement(json, "without"));
      return new IngredientWithout(base, without);
    }

    @Override
    public IngredientWithout parse(PacketByteBuf buffer) {
      Ingredient base = Ingredient.fromPacket(buffer);
      Ingredient without = Ingredient.fromPacket(buffer);
      return new IngredientWithout(base, without);
    }

    @Override
    public void write(PacketByteBuf buffer, IngredientWithout ingredient) {
      CraftingHelper.write(buffer, ingredient.base);
      CraftingHelper.write(buffer, ingredient.without);
    }
  }
}

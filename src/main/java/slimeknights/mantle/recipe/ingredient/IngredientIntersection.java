package slimeknights.mantle.recipe.ingredient;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
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

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Ingredient that matches anything that matches all of the sub ingredients */
public class IngredientIntersection extends Ingredient {
  public static final Identifier ID = Mantle.getResource("intersection");
  public static final IIngredientSerializer<IngredientIntersection> SERIALIZER = new Serializer();

  private final List<Ingredient> ingredients;
  private ItemStack[] intersectedMatchingStacks;
  private IntList packedMatchingStacks;
  public IngredientIntersection(List<Ingredient> ingredients) {
    super(Stream.empty());
    this.ingredients = ingredients;
  }

  public IngredientIntersection(Ingredient... ingredients) {
    this(ImmutableList.copyOf(ingredients));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return false;
    }
    for (Ingredient ingredient : ingredients) {
      if (!ingredient.test(stack)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack[] getMatchingStacksClient() {
    if (this.intersectedMatchingStacks == null) {
      if (ingredients.isEmpty()) {
        this.intersectedMatchingStacks = new ItemStack[0];
      } else {
        this.intersectedMatchingStacks = Arrays
          .stream(ingredients.get(0).getMatchingStacksClient())
          .filter(stack -> {
            for (int i = 1; i < ingredients.size(); i++) {
              if (!ingredients.get(i).test(stack)) {
                return false;
              }
            }
            return true;
          }).toArray(ItemStack[]::new);
      }
    }
    return intersectedMatchingStacks;
  }

  @Override
  public boolean isEmpty() {
    return getMatchingStacksClient().length == 0;
  }

  @Override
  public boolean isSimple() {
    for (Ingredient ingredient : ingredients) {
      if (!ingredient.isSimple()) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void invalidate() {
    super.invalidate();
    this.intersectedMatchingStacks = null;
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
    JsonArray array = new JsonArray();
    for (Ingredient ingredient : ingredients) {
      array.add(ingredient.toJson());
    }
    json.add("ingredients", array);
    return json;
  }

  @Override
  public IIngredientSerializer<IngredientIntersection> getSerializer() {
    return SERIALIZER;
  }

  private static class Serializer implements IIngredientSerializer<IngredientIntersection> {
    @Override
    public IngredientIntersection parse(JsonObject json) {
      List<Ingredient> ingredients = JsonHelper.parseList(json, "ingredients", (element, name) -> Ingredient.fromJson(element));
      return new IngredientIntersection(ingredients);
    }

    @Override
    public IngredientIntersection parse(PacketByteBuf buffer) {
      int size = buffer.readVarInt();
      ImmutableList.Builder<Ingredient> builder = ImmutableList.builder();
      for (int i = 0; i < size; i++) {
        builder.add(Ingredient.fromPacket(buffer));
      }
      return new IngredientIntersection(builder.build());
    }

    @Override
    public void write(PacketByteBuf buffer, IngredientIntersection intersection) {
      buffer.writeVarInt(intersection.ingredients.size());
      for (Ingredient ingredient : intersection.ingredients) {
        CraftingHelper.write(buffer, ingredient);
      }
    }
  }
}

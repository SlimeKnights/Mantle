package slimeknights.mantle.recipe.crafting;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import slimeknights.mantle.util.JsonHelper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ShapedFallbackRecipe extends ShapedRecipe {

  /** Recipes to skip if they match */
  private final List<Identifier> alternatives;
  private List<CraftingRecipe> alternativeCache;

  /**
   * Main constructor, creates a recipe from all parameters
   * @param id             Recipe ID
   * @param group          Recipe group
   * @param width          Recipe width
   * @param height         Recipe height
   * @param ingredients    Recipe input ingredients
   * @param output         Recipe output
   * @param alternatives   List of recipe names to fail this match if they match
   */
  public ShapedFallbackRecipe(Identifier id, String group, int width, int height, DefaultedList<Ingredient> ingredients, ItemStack output, List<Identifier> alternatives) {
    super(id, group, width, height, ingredients, output);
    this.alternatives = alternatives;
  }

  /**
   * Creates a recipe using a shaped recipe as a base
   * @param base          Shaped recipe to copy data from
   * @param alternatives  List of recipe names to fail this match if they match
   */
  public ShapedFallbackRecipe(ShapedRecipe base, List<Identifier> alternatives) {
    this(base.getId(), base.getGroup(), base.getWidth(), base.getHeight(), base.getPreviewInputs(), base.getOutput(), alternatives);
  }

  @Override
  public boolean matches(CraftingInventory inv, World world) {
    // if this recipe does not match, fail it
    if (!super.matches(inv, world)) {
      return false;
    }

    // fetch all alternatives, fail if any match
    // cache to save effort down the line
    if (alternativeCache == null) {
      RecipeManager manager = world.getRecipeManager();
      alternativeCache = alternatives.stream()
                                     .map(manager::get)
                                     .filter(Optional::isPresent)
                                     .map(Optional::get)
                                     .filter(recipe -> {
                                       // only allow exact shaped or shapeless match, prevent infinite recursion due to complex recipes
                                       Class<?> clazz = recipe.getClass();
                                       return clazz == ShapedRecipe.class || clazz == ShapelessRecipe.class;
                                     })
                                     .map(recipe -> (CraftingRecipe) recipe).collect(Collectors.toList());
    }
    // fail if any alterntaive matches
    return this.alternativeCache.stream().noneMatch(recipe -> recipe.matches(inv, world));
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return RecipeSerializer.SHAPED;
  }

  public static class Serializer extends ShapedRecipe.Serializer {
    @Override
    public ShapedFallbackRecipe read(Identifier id, JsonObject json) {
      ShapedRecipe base = super.read(id, json);
      List<Identifier> alternatives = JsonHelper.parseList(json, "alternatives", (element, name) -> new Identifier(net.minecraft.util.JsonHelper.asString(element, name)));
      return new ShapedFallbackRecipe(base, alternatives);
    }

    @Override
    public ShapedFallbackRecipe read(Identifier id, PacketByteBuf buffer) {
      ShapedRecipe base = super.read(id, buffer);
      assert base != null;
      int size = buffer.readVarInt();
      ImmutableList.Builder<Identifier> builder = ImmutableList.builder();
      for (int i = 0; i < size; i++) {
        builder.add(buffer.readIdentifier());
      }
      return new ShapedFallbackRecipe(base, builder.build());
    }

    @Override
    public void write(PacketByteBuf buffer, ShapedRecipe recipe) {
      // write base recipe
      super.write(buffer, recipe);
      // write extra data
      assert recipe instanceof ShapedFallbackRecipe;
      List<Identifier> alternatives = ((ShapedFallbackRecipe) recipe).alternatives;
      buffer.writeVarInt(alternatives.size());
      for (Identifier alternative : alternatives) {
        buffer.writeIdentifier(alternative);
      }
    }
  }
}

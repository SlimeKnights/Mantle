package slimeknights.mantle.recipe.crafting;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.MantleRecipeSerializers;
import slimeknights.mantle.util.JsonHelper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ShapedFallbackRecipe extends ShapedRecipe {

  /** Recipes to skip if they match */
  private final List<ResourceLocation> alternatives;
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
  public ShapedFallbackRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> ingredients, ItemStack output, List<ResourceLocation> alternatives) {
    super(id, group, width, height, ingredients, output);
    this.alternatives = alternatives;
  }

  /**
   * Creates a recipe using a shaped recipe as a base
   * @param base          Shaped recipe to copy data from
   * @param alternatives  List of recipe names to fail this match if they match
   */
  public ShapedFallbackRecipe(ShapedRecipe base, List<ResourceLocation> alternatives) {
    this(base.getId(), base.getGroup(), base.getWidth(), base.getHeight(), base.getIngredients(), base.getResultItem(), alternatives);
  }

  @Override
  public boolean matches(CraftingContainer inv, Level world) {
    // if this recipe does not match, fail it
    if (!super.matches(inv, world)) {
      return false;
    }

    // fetch all alternatives, fail if any match
    // cache to save effort down the line
    if (alternativeCache == null) {
      RecipeManager manager = world.getRecipeManager();
      alternativeCache = alternatives.stream()
                                     .map(manager::byKey)
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
    return MantleRecipeSerializers.CRAFTING_SHAPED_FALLBACK;
  }

  public static class Serializer extends ShapedRecipe.Serializer {
    @Override
    public ShapedFallbackRecipe fromJson(ResourceLocation id, JsonObject json) {
      ShapedRecipe base = super.fromJson(id, json);
      List<ResourceLocation> alternatives = JsonHelper.parseList(json, "alternatives", (element, name) -> new ResourceLocation(GsonHelper.convertToString(element, name)));
      return new ShapedFallbackRecipe(base, alternatives);
    }

    @Override
    public ShapedFallbackRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
      ShapedRecipe base = super.fromNetwork(id, buffer);
      assert base != null;
      int size = buffer.readVarInt();
      ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
      for (int i = 0; i < size; i++) {
        builder.add(buffer.readResourceLocation());
      }
      return new ShapedFallbackRecipe(base, builder.build());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ShapedRecipe recipe) {
      // write base recipe
      super.toNetwork(buffer, recipe);
      // write extra data
      assert recipe instanceof ShapedFallbackRecipe;
      List<ResourceLocation> alternatives = ((ShapedFallbackRecipe) recipe).alternatives;
      buffer.writeVarInt(alternatives.size());
      for (ResourceLocation alternative : alternatives) {
        buffer.writeResourceLocation(alternative);
      }
    }
  }
}

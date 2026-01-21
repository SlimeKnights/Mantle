package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.helper.LoggingRecipeSerializer;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.Map;

/** Recipe which sets the texture for a {@link slimeknights.mantle.block.RetexturedBlock} based on an ingredient input. */
// TODO 1.21: rework to be more like the ShapedMaterialsRecipe from Tinkers for more efficient network syncing
@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  /** Ingredient used to determine the texture on the output */
  @Getter
  private final Ingredient texture;
  private final boolean matchAll;

  /** Creates a new recipe using the passed parameters */
  protected ShapedRetexturedRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification, Ingredient texture, boolean matchAll) {
    super(id, group, category, width, height, ingredients, result, showNotification);
    this.texture = texture;
    this.matchAll = matchAll;
  }

  /**
   * Creates a new recipe using an existing shaped recipe
   * @param orig       Shaped recipe to copy
   * @param texture    Ingredient to use for the texture
   * @param matchAll   If true, all inputs must match for the recipe to match
   */
  protected ShapedRetexturedRecipe(ShapedRecipe orig, Ingredient texture, boolean matchAll) {
    this(orig.getId(), orig.getGroup(), orig.category(), orig.getWidth(), orig.getHeight(), orig.getIngredients(), orig.result, orig.showNotification(), texture, matchAll);
  }

  /**
   * Gets the output using the given texture
   * @param texture  Texture to use
   * @return  Output with texture. Will be blank if the input is not a block
   */
  public ItemStack getResultItem(Item texture, RegistryAccess access) {
    return RetexturedHelper.setTexture(getResultItem(access).copy(), Block.byItem(texture));
  }

  @Override
  public ItemStack assemble(CraftingContainer craftMatrix, RegistryAccess access) {
    ItemStack result = super.assemble(craftMatrix, access);
    Block currentTexture = null;
    for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
      ItemStack stack = craftMatrix.getItem(i);
      if (!stack.isEmpty() && texture.test(stack)) {
        // fetch texture from the block if it has one
        Block block = RetexturedHelper.getTexture(stack);
        // assuming it does not, use the block itself as the texture (provided it is not the result that is)
        if (block == Blocks.AIR && stack.getItem() != result.getItem()) {
          block = Block.byItem(stack.getItem());
        }
        // if no texture, skip
        if (block == Blocks.AIR) {
          continue;
        }

        // if we have not found a texture yet, store the found block
        if (currentTexture == null) {
          currentTexture = block;
          // match all means we must check the rest. If not match all, we can be done
          if (!matchAll) {
            break;
          }

          // if we found a texture before, must match or we do no texture
        } else if (currentTexture != block) {
          currentTexture = null;
          break;
        }
      }
    }

    // set the texture if found. No texture will use the fallback
    if (currentTexture != null) {
      return RetexturedHelper.setTexture(result, currentTexture);
    }
    return result;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_RETEXTURED.get();
  }

  public static class Serializer implements LoggingRecipeSerializer<ShapedRetexturedRecipe> {
    @Override
    public ShapedRetexturedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
      String group = GsonHelper.getAsString(json, "group", "");
      CraftingBookCategory category = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
      Map<String, Ingredient> key = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
      String[] pattern = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
      int width = pattern[0].length();
      int height = pattern.length;
      NonNullList<Ingredient> inputs = ShapedRecipe.dissolvePattern(pattern, key, width, height);
      ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
      boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);

      // fetch the texture from the map if its a primitive
      JsonElement textureElement = JsonHelper.getElement(json, "texture");
      Ingredient texture;
      if (textureElement.isJsonPrimitive()) {
        String textureKey = textureElement.getAsString();
        if (textureKey.length() != 1) {
          throw new JsonSyntaxException("Invalid texture key: '" + textureKey + "' is an invalid symbol (must be 1 character only).");
        }
        texture = key.get(textureKey);
        if (texture == null || texture == Ingredient.EMPTY) {
          throw new JsonSyntaxException("Texture ingredient references symbol '" + textureKey + "' but it's not defined in the key");
        }
      } else {
        // if it's an object or array, treat as an ingredient object
        texture = CraftingHelper.getIngredient(textureElement, false);
        Mantle.logger.warn("Using deprecated ingredient format on 'texture' for `mantle:crafting_shaped_retextured`. Use key instead.");
      }
      boolean matchAll = false;
      if (json.has("match_all")) {
        matchAll = json.get("match_all").getAsBoolean();
      }
      return new ShapedRetexturedRecipe(recipeId, group, category, width, height, inputs, result, showNotification, texture, matchAll);
    }

    @Nullable
    @Override
    public ShapedRetexturedRecipe fromNetworkSafe(ResourceLocation recipeId, FriendlyByteBuf buffer) {
      ShapedRecipe recipe = SHAPED_RECIPE.fromNetwork(recipeId, buffer);
      return recipe == null ? null : new ShapedRetexturedRecipe(recipe, Ingredient.fromNetwork(buffer), buffer.readBoolean());
    }

    @Override
    public void toNetworkSafe(FriendlyByteBuf buffer, ShapedRetexturedRecipe recipe) {
      SHAPED_RECIPE.toNetwork(buffer, recipe);
      recipe.texture.toNetwork(buffer);
      buffer.writeBoolean(recipe.matchAll);
    }
  }
}

package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import slimeknights.mantle.recipe.MantleRecipeSerializers;
import slimeknights.mantle.recipe.helper.LoggingRecipeSerializer;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  /** Ingredient used to determine the texture on the output */
  @Getter
  private final Ingredient texture;
  private final boolean matchAll;

  /**
   * Creates a new recipe using an existing shaped recipe
   * @param orig       Shaped recipe to copy
   * @param texture    Ingredient to use for the texture
   * @param matchAll   If true, all inputs must match for the recipe to match
   */
  protected ShapedRetexturedRecipe(ShapedRecipe orig, Ingredient texture, boolean matchAll) {
    super(orig.getId(), orig.getGroup(), orig.category(), orig.getWidth(), orig.getHeight(), orig.getIngredients(), orig.result);
    this.texture = texture;
    this.matchAll = matchAll;
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
        // if the item is the same as the result, copy the texture over
        Block block;
        if (stack.getItem() == result.getItem()) {
          block = RetexturedHelper.getTexture(stack);
        } else {
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
    return MantleRecipeSerializers.CRAFTING_SHAPED_RETEXTURED;
  }

  public static class Serializer implements LoggingRecipeSerializer<ShapedRetexturedRecipe> {
    @Override
    public ShapedRetexturedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
      ShapedRecipe recipe = SHAPED_RECIPE.fromJson(recipeId, json);
      Ingredient texture = CraftingHelper.getIngredient(JsonHelper.getElement(json, "texture"), false);
      boolean matchAll = false;
      if (json.has("match_all")) {
        matchAll = json.get("match_all").getAsBoolean();
      }
      return new ShapedRetexturedRecipe(recipe, texture, matchAll);
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

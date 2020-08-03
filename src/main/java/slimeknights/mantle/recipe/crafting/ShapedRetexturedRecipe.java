package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.recipe.MantleRecipeSerializers;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  private final Ingredient texture;
  private final boolean matchAll;

  /**
   * Creates a new recipe using an existing shaped recipe
   * @param orig       Shaped recipe to copy
   * @param texture    Ingredient to use for the texture
   * @param matchAll   If true, all inputs must match for the recipe to match
   */
  protected ShapedRetexturedRecipe(ShapedRecipe orig, Ingredient texture, boolean matchAll) {
    super(orig.getId(), orig.getGroup(), orig.getWidth(), orig.getHeight(), orig.getIngredients(), orig.getRecipeOutput());
    this.texture = texture;
    this.matchAll = matchAll;
  }

  @Override
  public ItemStack getCraftingResult(CraftingInventory craftMatrix) {
    ItemStack result = super.getCraftingResult(craftMatrix);
    Block currentTexture = null;
    for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
      ItemStack stack = craftMatrix.getStackInSlot(i);
      if (!stack.isEmpty() && texture.test(stack)) {
        // if the item is the same as the result, copy the texture over
        Block block;
        if (stack.getItem() == result.getItem()) {
          block = RetexturedBlockItem.getTexture(stack);
        } else {
          block = Block.getBlockFromItem(stack.getItem());
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
      return RetexturedBlockItem.setTexture(result, currentTexture);
    }
    return result;
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return MantleRecipeSerializers.CRAFTING_SHAPED_RETEXTURED;
  }

  public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<ShapedRetexturedRecipe> {
    @Override
    public ShapedRetexturedRecipe read(ResourceLocation recipeId, JsonObject json) {
      ShapedRecipe recipe = CRAFTING_SHAPED.read(recipeId, json);
      Ingredient texture = CraftingHelper.getIngredient(JsonHelper.getElement(json, "texture"));
      boolean matchAll = false;
      if (json.has("match_all")) {
        matchAll = json.get("match_all").getAsBoolean();
      }
      return new ShapedRetexturedRecipe(recipe, texture, matchAll);
    }

    @Nullable
    @Override
    public ShapedRetexturedRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
      ShapedRecipe recipe = CRAFTING_SHAPED.read(recipeId, buffer);
      return recipe == null ? null : new ShapedRetexturedRecipe(recipe, Ingredient.read(buffer), buffer.readBoolean());
    }

    @Override
    public void write(PacketBuffer buffer, ShapedRetexturedRecipe recipe) {
      CRAFTING_SHAPED.write(buffer, recipe);
      recipe.texture.write(buffer);
      buffer.writeBoolean(recipe.matchAll);
    }
  }
}

package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nullable;
import java.util.Optional;

/** Recipe which sets the texture for a {@link slimeknights.mantle.block.RetexturedBlock} based on an ingredient input. */
@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  private static final HolderLookup.Provider EMPTY_PROVIDER = HolderLookup.Provider.create(java.util.stream.Stream.empty());

  /** Ingredient used to determine the texture on the output */
  @Getter
  private final Ingredient texture;
  private final boolean matchAll;
  private final ItemStack result;
  @Nullable
  private final ResourceLocation recipeId;

  /** Creates a new recipe using the passed parameters */
  protected ShapedRetexturedRecipe(@Nullable ResourceLocation id, String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification, Ingredient texture, boolean matchAll) {
    super(group, category, pattern, result, showNotification);
    this.texture = texture;
    this.matchAll = matchAll;
    this.result = result;
    this.recipeId = id;
  }

  /** Creates a new recipe using the passed parameters */
  protected ShapedRetexturedRecipe(@Nullable ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean showNotification, Ingredient texture, boolean matchAll) {
    this(id, group, category, new ShapedRecipePattern(width, height, ingredients, Optional.empty()), result, showNotification, texture, matchAll);
  }

  /**
   * Creates a new recipe using an existing shaped recipe
   * @param orig       Shaped recipe to copy
   * @param texture    Ingredient to use for the texture
   * @param matchAll   If true, all inputs must match for the recipe to match
   */
  protected ShapedRetexturedRecipe(ShapedRecipe orig, Ingredient texture, boolean matchAll) {
    this(null, orig.getGroup(), orig.category(), orig.pattern, orig.getResultItem(EMPTY_PROVIDER).copy(), orig.showNotification(), texture, matchAll);
  }

  /** Gets a best-effort recipe ID for legacy JEI hooks. */
  public ResourceLocation getRecipeId() {
    return recipeId == null ? Mantle.getResource("unknown_retextured_recipe") : recipeId;
  }

  /**
   * Gets the output using the given texture
   * @param texture  Texture to use
   * @return  Output with texture. Will be blank if the input is not a block
   */
  public ItemStack getResultItem(Item texture, HolderLookup.Provider access) {
    return RetexturedHelper.setTexture(getResultItem(access).copy(), Block.byItem(texture));
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    return result;
  }

  @Override
  public ItemStack assemble(CraftingInput craftMatrix, HolderLookup.Provider access) {
    ItemStack result = super.assemble(craftMatrix, access);
    Block currentTexture = null;
    for (int i = 0; i < craftMatrix.size(); i++) {
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

  public static class Serializer implements RecipeSerializer<ShapedRetexturedRecipe> {
    private static final Codec<ShapedRetexturedRecipe> JSON_CODEC = Codec.PASSTHROUGH.xmap(
      dynamic -> fromJson(dynamic.convert(JsonOps.INSTANCE).getValue().getAsJsonObject()),
      recipe -> new Dynamic<>(JsonOps.INSTANCE, toJson(recipe)));
    private static final MapCodec<ShapedRetexturedRecipe> CODEC = MapCodec.assumeMapUnsafe(JSON_CODEC);
    private static final StreamCodec<RegistryFriendlyByteBuf,ShapedRetexturedRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

    @Override
    public MapCodec<ShapedRetexturedRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,ShapedRetexturedRecipe> streamCodec() {
      return STREAM_CODEC;
    }

    private static ShapedRetexturedRecipe fromJson(JsonObject json) {
      String group = GsonHelper.getAsString(json, "group", "");
      CraftingBookCategory category = json.has("category")
        ? CraftingBookCategory.CODEC.parse(JsonOps.INSTANCE, json.get("category")).getOrThrow(JsonSyntaxException::new)
        : CraftingBookCategory.MISC;
      ShapedRecipePattern.Data data = ShapedRecipePattern.Data.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(JsonSyntaxException::new);
      ShapedRecipePattern pattern = ShapedRecipePattern.of(data.key(), data.pattern());
      ItemStack result = ItemStack.STRICT_CODEC.parse(JsonOps.INSTANCE, GsonHelper.getAsJsonObject(json, "result")).getOrThrow(JsonSyntaxException::new);
      boolean showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);

      // fetch the texture from the map if its a primitive
      JsonElement textureElement = JsonHelper.getElement(json, "texture");
      Ingredient texture;
      if (textureElement.isJsonPrimitive()) {
        String textureKey = textureElement.getAsString();
        if (textureKey.length() != 1) {
          throw new JsonSyntaxException("Invalid texture key: '" + textureKey + "' is an invalid symbol (must be 1 character only).");
        }
        texture = data.key().get(textureKey.charAt(0));
        if (texture == null || texture == Ingredient.EMPTY) {
          throw new JsonSyntaxException("Texture ingredient references symbol '" + textureKey + "' but it's not defined in the key");
        }
      } else {
        // if it's an object or array, treat as an ingredient object
        texture = IngredientLoadable.DISALLOW_EMPTY.convert(textureElement, "texture");
        Mantle.logger.warn("Using deprecated ingredient format on 'texture' for `mantle:crafting_shaped_retextured`. Use key instead.");
      }
      boolean matchAll = GsonHelper.getAsBoolean(json, "match_all", false);
      return new ShapedRetexturedRecipe(null, group, category, pattern, result, showNotification, texture, matchAll);
    }

    private static JsonObject toJson(ShapedRetexturedRecipe recipe) {
      JsonObject json = ShapedRecipe.Serializer.CODEC.codec().encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(JsonSyntaxException::new).getAsJsonObject();
      json.add("texture", IngredientLoadable.DISALLOW_EMPTY.serialize(recipe.texture));
      if (recipe.matchAll) {
        json.addProperty("match_all", true);
      }
      return json;
    }

    private static ShapedRetexturedRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
      String group = buffer.readUtf();
      CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
      ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
      ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
      boolean showNotification = buffer.readBoolean();
      Ingredient texture = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
      boolean matchAll = buffer.readBoolean();
      return new ShapedRetexturedRecipe(null, group, category, pattern, result, showNotification, texture, matchAll);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedRetexturedRecipe recipe) {
      buffer.writeUtf(recipe.getGroup());
      buffer.writeEnum(recipe.category());
      ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
      ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
      buffer.writeBoolean(recipe.showNotification());
      Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.texture);
      buffer.writeBoolean(recipe.matchAll);
    }
  }
}

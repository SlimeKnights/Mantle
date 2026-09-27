package slimeknights.mantle.recipe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.RetexturedHelper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/** Recipe which sets the texture for a {@link slimeknights.mantle.block.RetexturedBlock} based on an ingredient input. */
@SuppressWarnings("WeakerAccess")
public class ShapedRetexturedRecipe extends ShapedRecipe {
  /** Codec for JSON */
  public static final MapCodec<ShapedRetexturedRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
    Pattern.MAP_CODEC.forGetter(r -> r.pattern),
    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(Recipe::showNotification),
    Codec.BOOL.optionalFieldOf("match_all", false).forGetter(r -> r.matchAll)
  ).apply(builder, ShapedRetexturedRecipe::new));
  /** Codec for network */
  public static final StreamCodec<RegistryFriendlyByteBuf,ShapedRetexturedRecipe> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.STRING_UTF8, Recipe::getGroup,
    CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
    Pattern.STREAM_CODEC, r -> r.pattern,
    ItemStack.STREAM_CODEC, r -> r.result,
    ByteBufCodecs.BOOL, Recipe::showNotification,
    ByteBufCodecs.BOOL, r -> r.matchAll,
    ShapedRetexturedRecipe::new);

  private final Pattern pattern;
  private final boolean matchAll;

  /** Creates a new recipe using the passed parameters */
  protected ShapedRetexturedRecipe(String group, CraftingBookCategory category, Pattern pattern, ItemStack result, boolean showNotification, boolean matchAll) {
    super(group, category, pattern.pattern, result, showNotification);
    this.pattern = pattern;
    this.matchAll = matchAll;
  }

  /** Gets the texture ingredient for the recipe */
  public Ingredient getTexture() {
    return pattern.texture;
  }

  @Override
  public ItemStack assemble(CraftingInput input, Provider registries) {
    ItemStack result = super.assemble(input, registries);
    Block currentTexture = null;
    Ingredient texture = getTexture();
    for (int i = 0; i < input.size(); i++) {
      ItemStack stack = input.getItem(i);
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


  /* JEI */

  private List<ItemStack> displayOutputs;
  private int[] textureSlots;

  /** Gets the result item without any texture. */
  public ItemStack getPlainOutput() {
    return result;
  }

  /** Gets all variants of the output stack to display in JEI */
  public List<ItemStack> getDisplayOutputs() {
    if (displayOutputs == null) {
      displayOutputs = Arrays.stream(getTexture().getItems())
        .map(stack -> RetexturedHelper.setTexture(result.copy(), Block.byItem(stack.getItem())))
        .toList();
      if (displayOutputs.isEmpty()) {
        displayOutputs = List.of(result);
      }
    }

    return displayOutputs;
  }

  /** Gets a list of indices that contain the texture ingredient */
  public int[] getTextureSlots() {
    if (textureSlots == null) {
      List<Ingredient> inputs = getIngredients();
      Ingredient texture = getTexture();
      textureSlots = IntStream.range(0, inputs.size()).filter(i -> inputs.get(i) == texture).toArray();
    }
    return textureSlots;
  }


  /* Helpers */

  /** Data object holding information on the shaped pattern plus the texture ingredient */
  public record Pattern(ShapedRecipePattern pattern, Ingredient texture, char textureSymbol) {
    /** Codec for JSON parsing */
    public static final MapCodec<Pattern> MAP_CODEC = Data.MAP_CODEC.flatXmap(Pattern::unpack, pattern -> {
      if (pattern.textureSymbol == '\0') {
        return DataResult.error(() -> "Cannot encode unpacked recipe");
      }
      return pattern.pattern.data.map(data -> DataResult.success(new Pattern.Data(data, pattern.textureSymbol)))
        .orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe"));
    });
    /** Codec for byte buffer */
    public static final StreamCodec<RegistryFriendlyByteBuf, Pattern> STREAM_CODEC = StreamCodec.composite(
      ShapedRecipePattern.STREAM_CODEC, Pattern::pattern,
      ByteBufCodecs.VAR_INT, Pattern::textureIndex,
      (pattern, texture) -> {
        // select the ingredient from the list by index, using modulo if invalid
        List<Ingredient> ingredients = pattern.ingredients();
        return new Pattern(pattern, ingredients.isEmpty() ? Ingredient.EMPTY : ingredients.get(texture % ingredients.size()), '\0');
      }
    );

    /** Gets the index of the pattern ingredient */
    private int textureIndex() {
      List<Ingredient> ingredients = pattern.ingredients();
      // find the pattern in the ingredients list
      int index = ingredients.indexOf(texture);
      if (index != -1) {
        return index;
      }
      // if that fails, use the first non-empty ingredient
      for (int i = 0; i < ingredients.size(); i++) {
        if (ingredients.get(i) != Ingredient.EMPTY) {
          return i;
        }
      }
      // shouldn't fail, but on the chance it does, use 0
      return 0;
    }

    /** Unpacks the texture from the given data */
    private static DataResult<Pattern> unpack(Data data) {
      Ingredient texture = data.data.key().get(data.textureSymbol);
      if (texture == null) {
        return DataResult.error(() -> "Texture references symbol '" + data.textureSymbol + "' but its not defined in the key");
      }
      return ShapedRecipePattern.unpack(data.data).map(pattern -> new Pattern(pattern, texture, data.textureSymbol));
    }

    /** Raw data from the recipe instance */
    private record Data(ShapedRecipePattern.Data data, char textureSymbol) {
      public static final MapCodec<Data> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ShapedRecipePattern.Data.MAP_CODEC.forGetter(Data::data),
        ShapedRecipePattern.Data.SYMBOL_CODEC.fieldOf("texture").forGetter(Data::textureSymbol)
      ).apply(instance, Data::new));
    }
  }
}

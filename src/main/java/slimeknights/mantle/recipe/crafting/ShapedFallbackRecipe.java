package slimeknights.mantle.recipe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.ArrayList;
import java.util.List;

public class ShapedFallbackRecipe extends ShapedRecipe {
  /** Codec for JSON */
  public static final MapCodec<ShapedFallbackRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
    ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(Recipe::showNotification),
    ResourceLocation.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("alternatives").forGetter(r -> r.alternatives)
  ).apply(builder, ShapedFallbackRecipe::new));
  /** Codec for network */
  public static final StreamCodec<RegistryFriendlyByteBuf,ShapedFallbackRecipe> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.STRING_UTF8, Recipe::getGroup,
    CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
    ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
    ItemStack.STREAM_CODEC, r -> r.result,
    ByteBufCodecs.BOOL, Recipe::showNotification,
    ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.alternatives,
    ShapedFallbackRecipe::new);

  /** Recipes to skip if they match */
  private final List<ResourceLocation> alternatives;
  private List<CraftingRecipe> alternativeCache;

  /** Creates a recipe from the passed parameters */
  public ShapedFallbackRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification, List<ResourceLocation> alternatives) {
    super(group, category, pattern, result, showNotification);
    this.alternatives = alternatives;
  }

  /** Gets the alternative recipes */
  private List<CraftingRecipe> getAlternatives(Level level) {
    if (alternativeCache == null) {
      List<CraftingRecipe> alternatives = new ArrayList<>();
      RecipeManager manager = level.getRecipeManager();
      for (ResourceLocation id : this.alternatives) {
        RecipeHolder<CraftingRecipe> holder = manager.byKeyTyped(RecipeType.CRAFTING, id);
        if (holder != null) {
          alternatives.add(holder.value());
        }
      }
      alternativeCache = List.copyOf(alternatives);
    }
    return alternativeCache;
  }

  @Override
  public boolean matches(CraftingInput inv, Level world) {
    // if this recipe does not match, fail it
    if (!super.matches(inv, world)) {
      return false;
    }
    // fail if any alternative matches
    return getAlternatives(world).stream().noneMatch(recipe -> recipe.matches(inv, world));
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_FALLBACK.get();
  }
}

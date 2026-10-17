package slimeknights.mantle.recipe.crafting.fallback;

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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.List;

/** Standard shaped crafting recipe that fails if any of the fallback recipes matches. Used to allow adding specific recipes that are ignored by the general recipe. */
public class ShapedFallbackRecipe extends ShapedRecipe implements FallbackCraftingRecipe {
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
    MantleStreamCodecs.RESOURCE_LOCATION_LIST, r -> r.alternatives,
    ShapedFallbackRecipe::new);

  /** Recipes to skip if they match */
  private final List<ResourceLocation> alternatives;
  private List<CraftingRecipe> alternativeCache;

  /** Creates a recipe from the passed parameters */
  public ShapedFallbackRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification, List<ResourceLocation> alternatives) {
    super(group, category, pattern, result, showNotification);
    this.alternatives = alternatives;
  }

  @Override
  public List<CraftingRecipe> getAlternatives(Level level) {
    if (alternativeCache == null) {
      alternativeCache = FallbackCraftingRecipe.computeAlternatives(level, alternatives);
    }
    return alternativeCache;
  }

  @Override
  public boolean matches(CraftingInput inv, Level world) {
    return super.matches(inv, world) && matchesAlternatives(inv, world);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_FALLBACK.get();
  }
}

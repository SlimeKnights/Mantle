package slimeknights.mantle.recipe.crafting.fallback;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.List;

/** Standard shapeless crafting recipe that fails if any of the fallback recipes matches. Used to allow adding specific recipes that are ignored by the general recipe. */
public class ShapelessFallbackRecipe extends ShapelessRecipe implements FallbackCraftingRecipe {
  /** Codec for JSON */
  public static final MapCodec<ShapelessFallbackRecipe> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
    Codec.STRING.optionalFieldOf("group", "").forGetter(Recipe::getGroup),
    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
    MantleCodecs.SHAPELESS_INGREDIENTS.fieldOf("ingredients").forGetter(r -> r.ingredients),
    ResourceLocation.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("alternatives").forGetter(r -> r.alternatives)
  ).apply(builder, ShapelessFallbackRecipe::new));
  /** Codec for network */
  public static final StreamCodec<RegistryFriendlyByteBuf,ShapelessFallbackRecipe> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.STRING_UTF8, Recipe::getGroup,
    CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
    ItemStack.STREAM_CODEC, r -> r.result,
    MantleStreamCodecs.INGREDIENT_LIST, r -> r.ingredients,
    MantleStreamCodecs.RESOURCE_LOCATION_LIST, r -> r.alternatives,
    ShapelessFallbackRecipe::new);

  /** Recipes to skip if they match */
  private final List<ResourceLocation> alternatives;
  private List<CraftingRecipe> alternativeCache;

  public ShapelessFallbackRecipe(String group, CraftingBookCategory category, ItemStack result, List<Ingredient> ingredients, List<ResourceLocation> alternatives) {
    super(group, category, result, NonNullList.copyOf(ingredients));
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
    return MantleRecipes.CRAFTING_SHAPELESS_FALLBACK.get();
  }
}

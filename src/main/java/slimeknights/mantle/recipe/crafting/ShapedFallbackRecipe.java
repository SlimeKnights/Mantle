package slimeknights.mantle.recipe.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ShapedFallbackRecipe extends ShapedRecipe {
  private static final HolderLookup.Provider EMPTY_PROVIDER = HolderLookup.Provider.create(java.util.stream.Stream.empty());

  /** Recipes to skip if they match */
  private final List<ResourceLocation> alternatives;
  private List<CraftingRecipe> alternativeCache;
  private final ItemStack result;

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
  public ShapedFallbackRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> ingredients, ItemStack output, List<ResourceLocation> alternatives) {
    this(group, category, new ShapedRecipePattern(width, height, ingredients, Optional.empty()), output, true, alternatives);
  }

  public ShapedFallbackRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack output, boolean showNotification, List<ResourceLocation> alternatives) {
    super(group, category, pattern, output, showNotification);
    this.alternatives = alternatives;
    this.result = output;
  }

  /**
   * Creates a recipe using a shaped recipe as a base
   * @param base          Shaped recipe to copy data from
   * @param alternatives  List of recipe names to fail this match if they match
   */
  public ShapedFallbackRecipe(ShapedRecipe base, List<ResourceLocation> alternatives) {
    this(base.getGroup(), base.category(), base.pattern, base.getResultItem(EMPTY_PROVIDER).copy(), base.showNotification(), alternatives);
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return result;
  }

  @Override
  public boolean matches(CraftingInput inv, Level world) {
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
                                     .flatMap(Optional::stream)
                                     .map(RecipeHolder::value)
                                     .filter(recipe -> {
                                       // only allow exact shaped or shapeless match, prevent infinite recursion due to complex recipes
                                       Class<?> clazz = recipe.getClass();
                                       return clazz == ShapedRecipe.class || clazz == ShapelessRecipe.class;
                                     })
                                     .map(recipe -> (CraftingRecipe) recipe).collect(Collectors.toList());
    }
    // fail if any alternative matches
    return this.alternativeCache.stream().noneMatch(recipe -> recipe.matches(inv, world));
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_FALLBACK.get();
  }

  public static class Serializer implements RecipeSerializer<ShapedFallbackRecipe> {
    private static final Codec<ShapedFallbackRecipe> JSON_CODEC = Codec.PASSTHROUGH.xmap(
      dynamic -> fromJson(dynamic.convert(JsonOps.INSTANCE).getValue().getAsJsonObject()),
      recipe -> new Dynamic<>(JsonOps.INSTANCE, toJson(recipe)));
    private static final MapCodec<ShapedFallbackRecipe> CODEC = MapCodec.assumeMapUnsafe(JSON_CODEC);
    private static final StreamCodec<RegistryFriendlyByteBuf,ShapedFallbackRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

    @Override
    public MapCodec<ShapedFallbackRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf,ShapedFallbackRecipe> streamCodec() {
      return STREAM_CODEC;
    }

    private static ShapedFallbackRecipe fromJson(JsonObject json) {
      ShapedRecipe base = ShapedRecipe.Serializer.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(JsonSyntaxException::new);
      List<ResourceLocation> alternatives = JsonHelper.parseList(json, "alternatives", Loadables.RESOURCE_LOCATION);
      return new ShapedFallbackRecipe(base, alternatives);
    }

    private static JsonObject toJson(ShapedFallbackRecipe recipe) {
      JsonObject json = ShapedRecipe.Serializer.CODEC.codec().encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(JsonSyntaxException::new).getAsJsonObject();
      json.add("alternatives", Loadables.RESOURCE_LOCATION.list(0).serialize(recipe.alternatives));
      return json;
    }

    private static ShapedFallbackRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
      String group = buffer.readUtf();
      CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
      ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
      ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
      boolean showNotification = buffer.readBoolean();
      int size = buffer.readVarInt();
      List<ResourceLocation> builder = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        builder.add(buffer.readResourceLocation());
      }
      return new ShapedFallbackRecipe(group, category, pattern, result, showNotification, List.copyOf(builder));
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedFallbackRecipe recipe) {
      buffer.writeUtf(recipe.getGroup());
      buffer.writeEnum(recipe.category());
      ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
      ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
      buffer.writeBoolean(recipe.showNotification());
      buffer.writeVarInt(recipe.alternatives.size());
      for (ResourceLocation alternative : recipe.alternatives) {
        buffer.writeResourceLocation(alternative);
      }
    }
  }
}

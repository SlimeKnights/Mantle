package slimeknights.mantle.recipe.cooking;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.datafixers.util.Function6;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

import java.util.Optional;

/**
 * Builder for {@link SmeltingResultRecipe}, {@link BlastingResultRecipe}, {@link SmokingResultRecipe}, and {@link CampfireResultRecipe}.
 * Can also be used as a single builder for {@link SmeltingRecipe}, {@link BlastingRecipe}, {@link SmokingRecipe}, and {@link CampfireCookingRecipe)} to share fields.
 */
@SuppressWarnings("unused")
@CanIgnoreReturnValue
public class CookingRecipeBuilder<T extends CookingRecipeBuilder<T>> extends AbstractRecipeBuilder<T> {
  protected final ItemOutput result;
  protected float experience = 1.0f;
  protected int cookingTime = 200;
  protected Ingredient ingredient = Ingredient.EMPTY;
  protected CookingBookCategory category = CookingBookCategory.MISC;
  protected CookingType type = CookingType.SMELTING;
  protected boolean forceItemOutput = false;

  protected CookingRecipeBuilder(ItemOutput result) {
    this.result = result;
    // automatically set the category based on the result item, as long as it's not a tag
    // based off how furnace recipes set categories
    Optional<Item> optional = result.getValueOrTag().left();
    if (optional.isPresent()) {
      Item item = optional.get();
      if (item.components().has(DataComponents.FOOD)) {
        category = CookingBookCategory.FOOD;
      } else if (item instanceof BlockItem) {
        category = CookingBookCategory.BLOCKS;
      }
    }
  }

  /** Creates a new builder instance */
  public static CookingRecipeBuilder<?> builder(ItemOutput result) {
    return new CookingRecipeBuilder<>(result);
  }

  /** Creates a new builder instance */
  public static CookingRecipeBuilder<?> builder(ItemLike output, int amount) {
    return builder(ItemOutput.fromItem(output, amount));
  }

  /** Creates a new builder instance */
  public static CookingRecipeBuilder<?> builder(ItemLike output) {
    return builder(output, 1);
  }

  /** Creates a new builder instance for a tag with the given size */
  public static CookingRecipeBuilder<?> builder(TagKey<Item> result, int amount) {
    return builder(ItemOutput.fromTag(result, amount));
  }

  /** Creates a new builder instance for a tag with size of 1 */
  public static CookingRecipeBuilder<?> builder(TagKey<Item> result) {
    return builder(result, 1);
  }

  /** Gets this builder casted to the given type. */
  @Override
  @SuppressWarnings("unchecked")
  protected T self() {
    return (T) this;
  }

  /**
   * Sets the type of {@link #save(RecipeOutput, ResourceLocation)}.
   * Note you can also just directly use {@link #saveSmelting(RecipeOutput, ResourceLocation)}, {@link #saveBlasting(RecipeOutput, ResourceLocation)},
   * {@link #saveSmoking(RecipeOutput, ResourceLocation)}, and {@link #saveCampfire(RecipeOutput, ResourceLocation)} directly.
   */
  public T type(CookingType type) {
    this.type = type;
    return self();
  }

  /** Sets the recipe book category for this recipe. */
  public T category(CookingBookCategory category) {
    this.category = category;
    return self();
  }

  /** Sets the input ingredient */
  public T requires(Ingredient ingredient) {
    this.ingredient = ingredient;
    return self();
  }

  /** Sets the input ingredient */
  public T requires(ItemLike item) {
    return requires(Ingredient.of(item));
  }

  /** Sets the input ingredient */
  public T requires(TagKey<Item> tag) {
    return requires(Ingredient.of(tag));
  }

  /** Sets the XP gain from this recipe */
  public T experience(float experience) {
    this.experience = experience;
    return self();
  }

  /** Sets the cooking time for this recipe relative to smelting. Note its halved for {@link CookingType#BLASTING} and {@link CookingType#SMOKING} and tripled for {@link CookingType#CAMPFIRE} */
  public T cookingTime(int cookingTime) {
    this.cookingTime = cookingTime;
    return self();
  }

  /** Forces the recipe to serialize using the Mantle serializers with {@link ItemOutput} even if it could serialize as {@link ItemStack}. Useful in {@link slimeknights.mantle.recipe.data.mock.MockRecipeOutput}. */
  public T forceItemOutput() {
    this.forceItemOutput = true;
    return self();
  }


  /** Helper to save a recipe */
  protected T save(RecipeOutput output, ResourceLocation id, CookingType cookingType) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    output.accept(id, cookingType.create(group, category, ingredient, result, experience, cookingTime, forceItemOutput), buildOptionalAdvancement(output, id, "cooking"));
    return self();
  }

  /** Saves the smelting recipe */
  public T saveSmelting(RecipeOutput output, ResourceLocation id) {
    return save(output, id, CookingType.SMELTING);
  }

  /** Saves the blasting recipe */
  public T saveBlasting(RecipeOutput output, ResourceLocation id) {
    return save(output, id, CookingType.BLASTING);
  }

  /** Saves the smoking recipe */
  public T saveSmoking(RecipeOutput output, ResourceLocation id) {
    return save(output, id, CookingType.SMOKING);
  }

  /** Saves the campfire recipe */
  public T saveCampfire(RecipeOutput output, ResourceLocation id) {
    return save(output, id, CookingType.CAMPFIRE);
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.ITEM.getKey(result.get().getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    save(output, id, type);
  }

  /** Helper to construct recipes */
  @RequiredArgsConstructor
  public enum CookingType {
    SMELTING(SmeltingRecipe::new, SmeltingResultRecipe::new),
    BLASTING(BlastingRecipe::new, BlastingResultRecipe::new),
    SMOKING(SmokingRecipe::new, SmokingResultRecipe::new),
    CAMPFIRE(CampfireCookingRecipe::new, CampfireResultRecipe::new);

    private final Function6<String,CookingBookCategory,Ingredient,ItemStack,Float,Integer,Recipe<?>> vanillaConstructor;
    private final Function6<String,CookingBookCategory,Ingredient,ItemOutput,Float,Integer,Recipe<?>> mantleConstructor;

    /** Gets the cook time for the given serializer */
    public int getTime(int cookingTime) {
      if (this == CAMPFIRE) {
        return cookingTime * 3;
      }
      if (this == SMELTING) {
        return cookingTime;
      }
      // smoking and blasting
      return cookingTime / 2;
    }

    /** Creates a recipe of this type */
    public Recipe<?> create(String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookTime, boolean forceItemOutput) {
      // use vanilla constructor if possible
      if (forceItemOutput || result.getValueOrTag().left().isPresent()) {
        return vanillaConstructor.apply(group, category, ingredient, result.get(), experience, getTime(cookTime));
      } else {
        // use mantle if required
        return mantleConstructor.apply(group, category, ingredient, result, experience, getTime(cookTime));
      }
    }
  }
}

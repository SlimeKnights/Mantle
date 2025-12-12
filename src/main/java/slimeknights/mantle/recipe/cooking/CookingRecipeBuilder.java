package slimeknights.mantle.recipe.cooking;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.datafixers.util.Function7;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Builder for {@link SmeltingResultRecipe}, {@link BlastingResultRecipe}, {@link SmokingResultRecipe}, and {@link CampfireResultRecipe} */
@SuppressWarnings({"unchecked", "unused"})
@CanIgnoreReturnValue
public class CookingRecipeBuilder<T extends CookingRecipeBuilder<T>> extends AbstractRecipeBuilder<T> {
  protected final ItemOutput result;
  protected float experience = 1.0f;
  protected int cookingTime = 200;
  protected Ingredient ingredient = Ingredient.EMPTY;
  protected CookingBookCategory category = CookingBookCategory.MISC;
  protected CookingType type = CookingType.SMELTING;

  protected CookingRecipeBuilder(ItemOutput result) {
    this.result = result;
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


  /**
   * Sets the type of {@link #save(RecipeOutput, ResourceLocation)} for the sake of conditional recipes.
   * Note you can also just directly use {@link #saveSmelting(RecipeOutput, ResourceLocation)}, {@link #saveBlasting(RecipeOutput, ResourceLocation)},
   * {@link #saveSmoking(RecipeOutput, ResourceLocation)}, and {@link #saveCampfire(RecipeOutput, ResourceLocation)} directly.
   */
  public T type(CookingType type) {
    this.type = type;
    return (T) this;
  }

  /** Sets the input ingredient */
  public T requires(Ingredient ingredient) {
    this.ingredient = ingredient;
    return (T) this;
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
    return (T) this;
  }

  /** Sets the cooking time for this recipe relative to smelting. Note its halved for {@link CookingType#BLASTING} and {@link CookingType#SMOKING} and tripled for {@link CookingType#CAMPFIRE} */
  public T cookingTime(int cookingTime) {
    this.cookingTime = cookingTime;
    return (T) this;
  }


  /** Helper to save a recipe */
  @SuppressWarnings("unchecked")
  private <R extends Recipe<?>> T save(RecipeOutput recipeOutput, ResourceLocation id, Function7<String,CookingBookCategory,Ingredient,ItemOutput,Float,Integer,Void,R> constructor, int cookingTime) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    AdvancementHolder advancement = buildOptionalAdvancement(id, "cooking");
    R recipe = constructor.apply(group, category, ingredient, result, experience, cookingTime, null);
    recipeOutput.accept(id, recipe, advancement);
    return (T) this;
  }

  /** Saves the smelting recipe */
  public T saveSmelting(RecipeOutput recipeOutput, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    AdvancementHolder advancement = buildOptionalAdvancement(id, "cooking");
    recipeOutput.accept(id, new SmeltingResultRecipe(group, category, ingredient, result, experience, cookingTime), advancement);
    return (T) this;
  }

  /** Saves the blasting recipe */
  public T saveBlasting(RecipeOutput recipeOutput, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    AdvancementHolder advancement = buildOptionalAdvancement(id, "cooking");
    recipeOutput.accept(id, new BlastingResultRecipe(group, category, ingredient, result, experience, cookingTime / 2), advancement);
    return (T) this;
  }

  /** Saves the smoking recipe */
  public T saveSmoking(RecipeOutput recipeOutput, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    AdvancementHolder advancement = buildOptionalAdvancement(id, "cooking");
    recipeOutput.accept(id, new SmokingResultRecipe(group, category, ingredient, result, experience, cookingTime / 2), advancement);
    return (T) this;
  }

  /** Saves the campfire recipe */
  public T saveCampfire(RecipeOutput recipeOutput, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    AdvancementHolder advancement = buildOptionalAdvancement(id, "cooking");
    recipeOutput.accept(id, new CampfireResultRecipe(group, category, ingredient, result, experience, cookingTime * 3), advancement);
    return (T) this;
  }

  @Override
  public void save(RecipeOutput recipeOutput) {
    save(recipeOutput, Loadables.ITEM.getKey(result.get().getItem()));
  }

  @Override
  public void save(RecipeOutput recipeOutput, ResourceLocation id) {
    switch (type) {
      case SMELTING -> saveSmelting(recipeOutput, id);
      case BLASTING -> saveBlasting(recipeOutput, id);
      case SMOKING -> saveSmoking(recipeOutput, id);
      case CAMPFIRE -> saveCampfire(recipeOutput, id);
    }
  }

  /** Helper to change the cooking type in {@link #save(RecipeOutput, ResourceLocation)} for the sake of conditional recipes */
  public enum CookingType { SMELTING, BLASTING, SMOKING, CAMPFIRE }
}

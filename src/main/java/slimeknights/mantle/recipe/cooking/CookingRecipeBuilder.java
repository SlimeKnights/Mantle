package slimeknights.mantle.recipe.cooking;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.datafixers.util.Function6;
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
   * Sets the type of {@link #save(RecipeOutput, ResourceLocation)}.
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
  private <R extends Recipe<?>> T save(RecipeOutput output, ResourceLocation id, Function6<String,CookingBookCategory,Ingredient,ItemOutput,Float,Integer,R> constructor, int cookingTime) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    output.accept(id, constructor.apply(group, category, ingredient, result, experience, cookingTime), buildOptionalAdvancement(output, id, "cooking"));
    return (T) this;
  }

  /** Saves the smelting recipe */
  public T saveSmelting(RecipeOutput output, ResourceLocation id) {
    return save(output, id, SmeltingResultRecipe::new, cookingTime);
  }

  /** Saves the blasting recipe */
  public T saveBlasting(RecipeOutput output, ResourceLocation id) {
    return save(output, id, BlastingResultRecipe::new, cookingTime / 2);
  }

  /** Saves the smoking recipe */
  public T saveSmoking(RecipeOutput output, ResourceLocation id) {
    return save(output, id, SmokingResultRecipe::new, cookingTime / 2);
  }

  /** Saves the campfire recipe */
  public T saveCampfire(RecipeOutput output, ResourceLocation id) {
    return save(output, id, CampfireResultRecipe::new, cookingTime * 3);
  }

  @Override
  public void save(RecipeOutput output) {
    save(output, Loadables.ITEM.getKey(result.get().getItem()));
  }

  @Override
  public void save(RecipeOutput output, ResourceLocation id) {
    switch (type) {
      case SMELTING -> saveSmelting(output, id);
      case BLASTING -> saveBlasting(output, id);
      case SMOKING  -> saveSmoking(output, id);
      case CAMPFIRE -> saveCampfire(output, id);
    }
  }

  /** Helper to change the cooking type in {@link #save(RecipeOutput, ResourceLocation)} */
  public enum CookingType { SMELTING, BLASTING, SMOKING, CAMPFIRE }
}

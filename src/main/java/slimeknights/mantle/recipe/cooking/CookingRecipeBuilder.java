package slimeknights.mantle.recipe.cooking;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mojang.datafixers.util.Function7;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.common.ItemStackLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Builder for {@link SmeltingResultRecipe}, {@link BlastingResultRecipe}, {@link SmokingResultRecipe}, and {@link CampfireResultRecipe}.
 * Can also be used as a single builder for {@link SmeltingRecipe}, {@link BlastingRecipe}, {@link SmokingRecipe}, and {@link CampfireCookingRecipe )} to share fields.
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
   * Sets the type of {@link #save(Consumer, ResourceLocation)} for the sake of {@link net.minecraftforge.common.crafting.ConditionalRecipe}.
   * Note you can also just directly use {@link #saveSmelting(Consumer, ResourceLocation)}, {@link #saveBlasting(Consumer, ResourceLocation)},
   * {@link #saveSmoking(Consumer, ResourceLocation)}, and {@link #saveCampfire(Consumer, ResourceLocation)} directly.
   */
  public T type(CookingType type) {
    this.type = type;
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

  /** Forces the recipe to serialize using the Mantle serializers with {@link ItemOutput} even if it could serialize as {@link ItemStack}. */
  public T forceItemOutput() {
    this.forceItemOutput = true;
    return self();
  }


  /** Helper to save a recipe */
  private <R extends Recipe<?>> T save(Consumer<FinishedRecipe> consumer, ResourceLocation id, CookingType cookingType) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Ingredient must be set");
    }
    consumer.accept(cookingType.create(this, id, group, category, ingredient, result, experience, cookingTime, forceItemOutput, buildOptionalAdvancement(id, "cooking")));
    return self();
  }

  /** Saves the smelting recipe */
  public T saveSmelting(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    return save(consumer, id, CookingType.SMELTING);
  }

  /** Saves the blasting recipe */
  public T saveBlasting(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    return save(consumer, id, CookingType.BLASTING);
  }

  /** Saves the smoking recipe */
  public T saveSmoking(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    return save(consumer, id, CookingType.SMOKING);
  }

  /** Saves the campfire recipe */
  public T saveCampfire(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    return save(consumer, id, CookingType.CAMPFIRE);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    save(consumer, Loadables.ITEM.getKey(result.get().getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    save(consumer, id, type);
  }

  /** Loadable that can serialize a vanilla cooking recipe */
  @SuppressWarnings("DataFlowIssue")
  private static final RecordLoadable<AbstractCookingRecipe> VANILLA_LOADABLE = RecordLoadable.create(
    ContextKey.ID.requiredField(), LoadableRecipeSerializer.RECIPE_GROUP, CookingResultRecipe.CATEGORY_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.getIngredients().get(0)),
    ItemStackLoadable.REQUIRED_STACK_NBT.requiredField("result", r -> r.getResultItem(null)),
    CookingResultRecipe.EXPERIENCE_FIELD,
    IntLoadable.FROM_ONE.requiredField("cookingtime", AbstractCookingRecipe::getCookingTime),
    SmeltingRecipe::new);

  /** Helper to change the cooking type in {@link #save(Consumer, ResourceLocation)} for the sake of {@link net.minecraftforge.common.crafting.ConditionalRecipe} */
  @RequiredArgsConstructor
  public enum CookingType {
    SMELTING(SmeltingRecipe::new, SmeltingResultRecipe.LOADABLE, SmeltingResultRecipe::new),
    BLASTING(BlastingRecipe::new, BlastingResultRecipe.LOADABLE, BlastingResultRecipe::new),
    SMOKING(SmokingRecipe::new, SmokingResultRecipe.LOADABLE, SmokingResultRecipe::new),
    CAMPFIRE(CampfireCookingRecipe::new, CampfireResultRecipe.LOADABLE, CampfireResultRecipe::new);

    private final Function7<ResourceLocation,String,CookingBookCategory,Ingredient,ItemStack,Float,Integer,AbstractCookingRecipe> vanillaConstructor;
    private final RecordLoadable<? extends AbstractCookingRecipe> mantleLoadable;
    private final Function7<ResourceLocation,String,CookingBookCategory,Ingredient,ItemOutput,Float,Integer,AbstractCookingRecipe> mantleConstructor;

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
    @SuppressWarnings("unchecked")
    public FinishedRecipe create(AbstractRecipeBuilder<?> builder, ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookingTime, boolean forceItemOutput, @Nullable ResourceLocation advancementID) {
      if (forceItemOutput || result.getTag() == null) {
        return builder.new LoadableFinishedRecipe<>(vanillaConstructor.apply(id, group, category, ingredient, result.get(), experience, getTime(cookingTime)), VANILLA_LOADABLE, advancementID);
      } else {
        return builder.new LoadableFinishedRecipe<>(mantleConstructor.apply(id, group, category, ingredient, result, experience, getTime(cookingTime)), (RecordLoadable<AbstractCookingRecipe>) mantleLoadable, advancementID);
      }
    }
  }
}

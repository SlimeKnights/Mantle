package slimeknights.mantle.recipe.cooking;

import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Simplifies the serializers for result recipes */
public interface CookingResultRecipe {
  LoadableField<CookingBookCategory, AbstractCookingRecipe> CATEGORY_FIELD = new EnumLoadable<>(CookingBookCategory.class).defaultField("category", CookingBookCategory.MISC, true, AbstractCookingRecipe::category);
  LoadableField<Float, AbstractCookingRecipe> EXPERIENCE_FIELD = FloatLoadable.FROM_ZERO.defaultField("experience",0f, AbstractCookingRecipe::getExperience);
  LoadableField<ItemOutput,CookingResultRecipe> RESULT_FIELD = ItemOutput.Loadable.REQUIRED_STACK.requiredField("result", CookingResultRecipe::getResult);

  /** Gets the recipe result */
  ItemOutput getResult();
}

package slimeknights.mantle.plugin.jei.entity;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.plugin.jei.MantleJEIConstants;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;

import javax.annotation.Nullable;

/** Handler for working with entity types as ingredients */
public class EntityIngredientHelper implements IIngredientHelper<EntityIngredient.EntityInput> {
  @Override
  public IIngredientType<EntityIngredient.EntityInput> getIngredientType() {
    return MantleJEIConstants.ENTITY_TYPE;
  }

  @Override
  public String getDisplayName(EntityIngredient.EntityInput type) {
    return type.type().getDescription().getString();
  }

  @Override
  public String getUniqueId(EntityIngredient.EntityInput type, UidContext context) {
    return getResourceLocation(type).toString();
  }

  @Override
  public ResourceLocation getResourceLocation(EntityIngredient.EntityInput type) {
    return Registry.ENTITY_TYPE.getKey(type.type());
  }

  @Override
  public EntityIngredient.EntityInput copyIngredient(EntityIngredient.EntityInput type) {
    return type;
  }

  @Override
  public String getErrorInfo(@Nullable EntityIngredient.EntityInput type) {
    if (type == null) {
      return "null";
    }
    return getResourceLocation(type).toString();
  }
}

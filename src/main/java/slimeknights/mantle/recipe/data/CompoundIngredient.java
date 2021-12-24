package slimeknights.mantle.recipe.data;

import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

/**
 * Simply an extension of the forge class because the constructor is not public for some dumb reason
 */
public class CompoundIngredient extends net.minecraftforge.common.crafting.CompoundIngredient {
  protected CompoundIngredient(List<Ingredient> children) {
    super(children);
  }

  public static CompoundIngredient from(List<Ingredient> ingredients) {
    return new CompoundIngredient(ingredients);
  }

  public static CompoundIngredient from(Ingredient... ingredients) {
    return from(Arrays.asList(ingredients));
  }
}

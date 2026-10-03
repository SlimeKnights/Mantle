package slimeknights.mantle.recipe.crafting;

import lombok.experimental.Accessors;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Builder for recipes that extend {@link net.minecraft.world.item.crafting.ShapelessRecipe} or behave in a similar way. */
@SuppressWarnings("unused") // API
@Accessors(fluent = true)
public abstract class ShapelessExtensionBuilder<T extends ShapelessExtensionBuilder<T>> extends CraftingExtensionBuilder<T> {
  private final NonNullList<Ingredient> ingredients = NonNullList.create();

  protected ShapelessExtensionBuilder(ItemStack result) {
    super(result);
  }


  /** Requires the given ingredient */
  public T requires(Ingredient ingredient) {
    this.ingredients.add(ingredient);
    return self();
  }

  /** Requires multiple copies of the given ingredient */
  public T requires(Ingredient ingredient, int quantity) {
    for(int i = 0; i < quantity; ++i) {
      this.ingredients.add(ingredient);
    }
    return self();
  }

  /** Requires the given tag */
  public T requires(TagKey<Item> tag) {
    return this.requires(Ingredient.of(tag));
  }

  /** Requires multiple copies of the given tag */
  public T requires(TagKey<Item> tag, int quantity) {
    return this.requires(Ingredient.of(tag), quantity);
  }

  /** Requires the given item */
  public T requires(ItemLike item) {
    return this.requires(Ingredient.of(item));
  }

  /** Requires multiple copies of the given item */
  public T requires(ItemLike item, int quantity) {
    return this.requires(Ingredient.of(item), 1);
  }
}

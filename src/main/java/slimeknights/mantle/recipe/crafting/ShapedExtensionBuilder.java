package slimeknights.mantle.recipe.crafting;

import lombok.experimental.Accessors;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builder for recipes that extend {@link net.minecraft.world.item.crafting.ShapedRecipe} or behave in a similar way. */
@SuppressWarnings("unused") // API
@Accessors(fluent = true)
public abstract class ShapedExtensionBuilder<T extends ShapedExtensionBuilder<T>> extends CraftingExtensionBuilder<T> {
  protected RecipeCategory category = RecipeCategory.MISC;
  // recipe structure
  protected final List<String> rows = new ArrayList<>();
  protected final Map<Character, Ingredient> key = new LinkedHashMap<>();

  protected ShapedExtensionBuilder(ItemStack result) {
    super(result);
  }

  /**
   * Defines the given ingredient as the given symbol
   * @param symbol      Symbol
   * @param ingredient  Ingredient for the symbol
   * @return Builder instance.
   */
  public T define(Character symbol, Ingredient ingredient) {
    if (this.key.containsKey(symbol)) {
      throw new IllegalArgumentException("Symbol '" + symbol + "' is already defined!");
    }
    if (symbol == ' ') {
      throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
    }
    this.key.put(symbol, ingredient);
    return self();
  }

  /**
   * Defines the given ingredient as the given symbol
   * @param symbol  Symbol
   * @param tag     Tag for symbol
   * @return Builder instance.
   */
  public T define(Character symbol, TagKey<Item> tag) {
    return this.define(symbol, Ingredient.of(tag));
  }

  /**
   * Defines the given ingredient as the given symbol
   * @param symbol  Symbol
   * @param item    Item for symbol
   * @return Builder instance.
   */
  public T define(Character symbol, ItemLike item) {
    return this.define(symbol, Ingredient.of(item));
  }

  /**
   * Adds a pattern row
   * @param pattern  Pattern string. Should contain characters from {@link #define(Character, Ingredient)}
   * @return Builder instance
   */
  public T pattern(String pattern) {
    if (!this.rows.isEmpty() && pattern.length() != this.rows.getFirst().length()) {
      throw new IllegalArgumentException("Pattern must be the same width on every line!");
    }
    this.rows.add(pattern);
    return self();
  }

  /** Gets the pattern for this recipe. */
  protected ShapedRecipePattern getPattern() {
    return ShapedRecipePattern.of(this.key, this.rows);
  }
}

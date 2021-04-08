package slimeknights.mantle.recipe.data;

import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.crafting.conditions.ICondition;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utilities to help in the creation of recipes
 */
@SuppressWarnings("unused")
public interface IRecipeBuilderUtils {
  /**
   * Gets the mod ID for this recipe builder
   * @return  Mod ID
   */
  String getModId();

  /**
   * Gets the base recipe consumer
   * @return Base recipe consumer
   */
  Consumer<RecipeJsonProvider> getConsumer();

  /**
   * Gets the base condition for the condition utility
   */
  @Nullable
  default ICondition baseCondition() {
    return null;
  }

  /**
   * Gets a resource location under the Inspirations mod ID
   * @param name Resource path
   * @return Resource location for Inspirations
   */
  default Identifier resource(String name) {
    return new Identifier(getModId(), name);
  }

  /**
   * Gets a resource location string for the given path
   * @param name Resource path
   * @return Resource location string Inspirations
   */
  default String resourceName(String name) {
    return String.format("%s:%s", getModId(), name);
  }

  /**
   * Prefixes an items resource location with the given folder
   * @param item   Item to fetch resource location from
   * @param prefix Name to prefix location with
   * @return Prefixed resource location
   */
  default Identifier prefix(ItemConvertible item, String prefix) {
    return resource(prefix + Objects.requireNonNull(item.asItem().getRegistryName()).getPath());
  }

  /**
   * Wraps an items resource location with the given folder and suffix
   * @param item   Item to fetch resource location from
   * @param prefix Name to prefix location with
   * @param suffix Suffix for location
   * @return Prefixed resource location
   */
  default Identifier wrap(ItemConvertible item, String prefix, String suffix) {
    return resource(prefix + Objects.requireNonNull(item.asItem().getRegistryName()).getPath() + suffix);
  }

  /**
   * Gets a consumer with the given condition, plus the module condition
   * @param conditions Conditions to add
   * @return Consumer with condition
   */
  default Consumer<RecipeJsonProvider> withCondition(ICondition... conditions) {
    ConsumerWrapperBuilder builder = ConsumerWrapperBuilder.wrap();
    ICondition base = baseCondition();
    if (base != null) {
      builder.addCondition(base);
    }
    for (ICondition condition : conditions) {
      builder.addCondition(condition);
    }
    return builder.build(Objects.requireNonNull(getConsumer()));
  }
}

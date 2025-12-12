package slimeknights.mantle.recipe.helper;

import slimeknights.mantle.data.loadable.record.RecordLoadable;

/**
 * In NeoForge 1.21+, custom ingredient serialization has changed to use Codec/MapCodec-based
 * IngredientType instead of IIngredientSerializer. This class is deprecated and will be removed.
 * 
 * @deprecated Use IngredientType with MapCodec instead. See NeoForge DataComponentIngredient
 *             for the new pattern.
 */
@Deprecated(forRemoval = true)
public record LoadableIngredientSerializer<T>(RecordLoadable<T> loadable) {
  // This class no longer implements IIngredientSerializer as that interface was removed.
  // Kept as a placeholder to allow dependent code to compile during migration.
}

package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.LoadableCodecs;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ingredient that shows all potion variants on the displayed item list.
 */
public class PotionDisplayIngredient extends ItemIngredient {
  public static final RecordLoadable<PotionDisplayIngredient> LOADABLE = RecordLoadable.create(ItemsField.INSTANCE, TAG_FIELD, PotionDisplayIngredient::new);
  public static final MapCodec<PotionDisplayIngredient> CODEC = LoadableCodecs.mapCodec(LOADABLE);
  public static final IngredientType<PotionDisplayIngredient> TYPE = new IngredientType<>(CODEC);

  /** @deprecated Ingredient serializer was replaced by {@link #TYPE} in NeoForge 1.21+. */
  @Deprecated(forRemoval = true)
  public static final LoadableIngredientSerializer<PotionDisplayIngredient> SERIALIZER = new LoadableIngredientSerializer<>(LOADABLE);

  protected PotionDisplayIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    super(items, tag);
  }

  /** Creates a ingredient matching a list of items */
  public static PotionDisplayIngredient of(List<ItemLike> items) {
    return new PotionDisplayIngredient(toItem(items), null);
  }

  /** Creates a ingredient matching a list of items */
  public static PotionDisplayIngredient of(ItemLike... items) {
    return of(List.of(items));
  }

  /** Creates a ingredient matching a tag */
  public static PotionDisplayIngredient of(TagKey<Item> tag) {
    return new PotionDisplayIngredient(List.of(), tag);
  }

  /** Sets the potion on a stack using the new data component system */
  private static ItemStack setPotion(ItemStack stack, Potion potion) {
    ItemStack copy = stack.copy();
    copy.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    return copy;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public Stream<ItemStack> getItems() {
    // if empty, means we want wildcard, show all potions on the stack
    ItemStack[] parentStacks = getBaseStacks();
    if (parentStacks.length == 0) {
      return Stream.empty();
    }
    return BuiltInRegistries.POTION.stream()
      .filter(pot -> pot != Potions.WATER.value()) // EMPTY became WATER in 1.21
      .flatMap(pot -> Arrays.stream(parentStacks).map(item -> setPotion(item, pot)));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof PotionDisplayIngredient other && baseEquals(other);
  }

  @Override
  public int hashCode() {
    return baseHashCode();
  }
}

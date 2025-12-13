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
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.LoadableCodecs;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableIngredientSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Ingredient checking for an item with a specific potion.
 */
public class PotionIngredient extends ItemIngredient {
  public static final RecordLoadable<PotionIngredient> LOADABLE = RecordLoadable.create(
    ItemsField.INSTANCE, TAG_FIELD,
    Loadables.POTION.defaultField("potion", Potions.WATER.value(), false, i -> i.potion),
    PotionIngredient::new
  );
  public static final MapCodec<PotionIngredient> CODEC = LoadableCodecs.mapCodec(LOADABLE);
  public static final IngredientType<PotionIngredient> TYPE = new IngredientType<>(CODEC);

  /** @deprecated Ingredient serializer was replaced by {@link #TYPE} in NeoForge 1.21+. */
  @Deprecated(forRemoval = true)
  public static final LoadableIngredientSerializer<PotionIngredient> SERIALIZER = new LoadableIngredientSerializer<>(LOADABLE);

  private final Potion potion;
  
  protected PotionIngredient(List<Item> items, @Nullable TagKey<Item> itemTag, Potion potion) {
    super(items, itemTag);
    this.potion = potion;
  }

  /** Creates a potion ingredient matching a list of items */
  public static PotionIngredient of(Potion potion, List<ItemLike> items) {
    return new PotionIngredient(toItem(items), null, potion);
  }

  /** Creates a potion ingredient matching a list of items */
  public static PotionIngredient of(Potion potion, ItemLike... items) {
    return of(potion, Arrays.asList(items));
  }

  /** Creates a potion ingredient matching a tag */
  public static PotionIngredient of(Potion potion, TagKey<Item> tag) {
    return new PotionIngredient(List.of(), tag, potion);
  }

  /** Gets the potion from a stack using the new data component system */
  private static Optional<Potion> getPotionFromStack(ItemStack stack) {
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    if (contents != null && contents.potion().isPresent()) {
      return contents.potion().map(holder -> holder.value());
    }
    return Optional.empty();
  }

  /** Sets the potion on a stack using the new data component system */
  private static ItemStack setPotion(ItemStack stack, Potion potion) {
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    return stack;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    if (!super.test(stack)) {
      return false;
    }
    return getPotionFromStack(stack).map(p -> p == potion).orElse(false);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return super.getItems().map(stack -> setPotion(stack.copy(), potion));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    return obj instanceof PotionIngredient other && baseEquals(other) && potion == other.potion;
  }

  @Override
  public int hashCode() {
    return 31 * baseHashCode() + potion.hashCode();
  }
}

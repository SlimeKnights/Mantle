package slimeknights.mantle.recipe.ingredient.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.List;
import java.util.stream.Stream;

/**
 * Ingredient that shows all potion variants on the displayed item list
 * @see PotionIngredient
 */
public record PotionDisplayIngredient(IngredientItems items) implements ICustomIngredient {
  public static final MapCodec<PotionDisplayIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    IngredientItems.CODEC.forGetter(PotionDisplayIngredient::items)
  ).apply(instance, PotionDisplayIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,PotionDisplayIngredient> STREAM_CODEC = IngredientItems.STREAM_CODEC.map(PotionDisplayIngredient::new, PotionDisplayIngredient::items);

  /** Creates an ingredient matching a list of items */
  public static Ingredient of(List<ItemLike> items) {
    return new PotionDisplayIngredient(IngredientItems.of(items)).toVanilla();
  }

  /** Creates an ingredient matching a list of items */
  public static Ingredient of(ItemLike... items) {
    return of(List.of(items));
  }

  /** Creates an ingredient matching a tag */
  public static Ingredient of(TagKey<Item> tag) {
    return new PotionDisplayIngredient(IngredientItems.of(tag)).toVanilla();
  }

  @Override
  public IngredientType<PotionDisplayIngredient> getType() {
    return MantleRecipes.POTION_DISPLAY_INGREDIENT.get();
  }

  @Override
  public boolean test(ItemStack stack) {
    return items.test(stack);
  }

  @Override
  public boolean isSimple() {
    // TODO: this causes it to sync as all the items, is that fine?
    return true;
  }

  @Override
  public Stream<ItemStack> getItems() {
    List<Item> items = this.items.getAllItems();
    return BuiltInRegistries.POTION.holders()
      .flatMap(potion -> items.stream().map(item -> {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
      }));
  }
}

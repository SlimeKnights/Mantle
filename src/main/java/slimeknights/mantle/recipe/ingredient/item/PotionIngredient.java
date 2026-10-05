package slimeknights.mantle.recipe.ingredient.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.network.MantleStreamCodecs;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.List;
import java.util.stream.Stream;

/**
 * Simple ingredient checking for an item with a specific potion.
 * @see PotionDisplayIngredient
 * @see slimeknights.mantle.recipe.ingredient.fluid.PotionFluidIngredient
 */
public record PotionIngredient(Holder<Potion> potion, IngredientItems items) implements ICustomIngredient {
  public static final MapCodec<PotionIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    BuiltInRegistries.POTION.holderByNameCodec().fieldOf("potion").forGetter(PotionIngredient::potion),
    IngredientItems.CODEC.forGetter(PotionIngredient::items)
  ).apply(instance, PotionIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf,PotionIngredient> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.POTION, PotionIngredient::potion,
    IngredientItems.STREAM_CODEC, PotionIngredient::items,
    PotionIngredient::new);

  /** Creates a potion ingredient matching a list of items */
  public static Ingredient of(Holder<Potion> potion, List<ItemLike> items) {
    return new PotionIngredient(potion, IngredientItems.of(items)).toVanilla();
  }

  /** Creates a potion ingredient matching a list of items */
  public static Ingredient of(Holder<Potion> potion, ItemLike... items) {
    return of(potion, List.of(items));
  }

  /** Creates a potion ingredient matching a tag */
  public static Ingredient of(Holder<Potion> potion, TagKey<Item> tag) {
    return new PotionIngredient(potion, IngredientItems.of(tag)).toVanilla();
  }

  @Override
  public IngredientType<PotionIngredient> getType() {
    return MantleRecipes.POTION_INGREDIENT.get();
  }

  @Override
  public boolean test(ItemStack stack) {
    // must match an item, and potion must match
    return items.test(stack) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(potion);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public Stream<ItemStack> getItems() {
    PotionContents contents = new PotionContents(potion);
    return items.getAllItems().stream().map(item -> {
      ItemStack stack = new ItemStack(item);
      stack.set(DataComponents.POTION_CONTENTS, contents);
      return stack;
    });
  }
}

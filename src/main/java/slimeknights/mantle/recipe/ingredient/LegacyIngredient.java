package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.compat.neoforged.neoforge.common.crafting.AbstractIngredient;
import slimeknights.mantle.compat.neoforged.neoforge.common.crafting.IIngredientSerializer;

/** Adapts Mantle's old Forge ingredient classes to NeoForge's custom ingredient API. */
public record LegacyIngredient<T extends AbstractIngredient>(T ingredient, Supplier<? extends IngredientType<?>> type) implements ICustomIngredient {
  public static <T extends AbstractIngredient> IngredientType<LegacyIngredient<T>> type(IIngredientSerializer<T> serializer, Supplier<? extends IngredientType<?>> type) {
    return new IngredientType<>(codec(serializer, type), streamCodec(serializer, type));
  }

  private static <T extends AbstractIngredient> MapCodec<LegacyIngredient<T>> codec(IIngredientSerializer<T> serializer, Supplier<? extends IngredientType<?>> type) {
    return serializer.codec().xmap(ingredient -> new LegacyIngredient<>(ingredient, type), LegacyIngredient::ingredient);
  }

  private static <T extends AbstractIngredient> StreamCodec<RegistryFriendlyByteBuf,LegacyIngredient<T>> streamCodec(IIngredientSerializer<T> serializer, Supplier<? extends IngredientType<?>> type) {
    return StreamCodec.of(
      (buffer, ingredient) -> serializer.write(buffer, ingredient.ingredient()),
      buffer -> new LegacyIngredient<>(serializer.parse(buffer), type)
    );
  }

  @Override
  public boolean test(ItemStack stack) {
    return ingredient.test(stack);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return Arrays.stream(ingredient.getItems());
  }

  @Override
  public boolean isSimple() {
    return ingredient.isSimple();
  }

  @Override
  public IngredientType<?> getType() {
    return type.get();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof LegacyIngredient<?> that && ingredient.equals(that.ingredient) && type.get().equals(that.type.get());
  }

  @Override
  public int hashCode() {
    return Objects.hash(ingredient, type.get());
  }
}

package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.data.SupplierStreamCodec;

import java.util.function.Supplier;

/** Simple implementation of a recipe serializer from a map codec and a codec */
public record SimpleRecipeSerializer<T extends Recipe<?>>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf,T> streamCodec) implements RecipeSerializer<T> {
  /** Creates a recipe serializer for a constructor which is called every time data reloads */
  public static <T extends Recipe<?>> SimpleRecipeSerializer<T> noArguments(Supplier<T> constructor) {
    return new SimpleRecipeSerializer<>(MapCodec.unit(constructor), new SupplierStreamCodec<>(constructor));
  }

  /** Creates a recipe serializer for a singleton instance which is used every time. */
  public static <T extends Recipe<?>> SimpleRecipeSerializer<T> singleton(T value) {
    return new SimpleRecipeSerializer<>(MapCodec.unit(value), StreamCodec.unit(value));
  }
}

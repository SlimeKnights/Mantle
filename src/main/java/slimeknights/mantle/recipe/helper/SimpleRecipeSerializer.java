package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.Mantle;

import java.lang.reflect.Method;
import java.util.function.Function;

/** Simple implementation of a recipe serializer with no properties other than recipe ID. */
public record SimpleRecipeSerializer<T extends Recipe<?>>(Function<ResourceLocation,T> constructor) implements RecipeSerializer<T> {
  @Override
  public MapCodec<T> codec() {
    return RecordCodecBuilder.mapCodec(instance -> instance.group(
      ResourceLocation.CODEC.optionalFieldOf("id", Mantle.getResource("unknown_simple_recipe")).forGetter(SimpleRecipeSerializer::getRecipeId)
    ).apply(instance, constructor));
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf,T> streamCodec() {
    return StreamCodec.of((buffer, recipe) -> buffer.writeResourceLocation(getRecipeId(recipe)), buffer -> constructor.apply(buffer.readResourceLocation()));
  }

  /** Gets the recipe ID from legacy recipe classes for 1.21 recipe packets. */
  private static ResourceLocation getRecipeId(Recipe<?> recipe) {
    try {
      Method method = recipe.getClass().getMethod("getId");
      if (method.invoke(recipe) instanceof ResourceLocation id) {
        return id;
      }
    } catch (ReflectiveOperationException e) {
      return Mantle.getResource("unknown_simple_recipe");
    }
    return Mantle.getResource("unknown_simple_recipe");
  }
}

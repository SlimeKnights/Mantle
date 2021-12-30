package slimeknights.mantle.recipe.helper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;

/**
 * Recipe serializer that logs exceptions before throwing them as otherwise the exceptions may be invisible
 * @param <T>  Recipe class
 */
public abstract class LoggingRecipeSerializer<T extends Recipe<?>> extends AbstractRecipeSerializer<T> {
  /**
   * Read the recipe from the packet
   * @param id      Recipe ID
   * @param buffer  Buffer instance
   * @return  Parsed recipe
   * @throws RuntimeException  If any errors happen, the exception will be logged automatically
   */
  @Nullable
  protected abstract T fromNetworkSafe(ResourceLocation id, FriendlyByteBuf buffer);

  /**
   * Write the method to the buffer
   * @param buffer  Buffer instance
   * @param recipe  Recipe instance
   * @throws RuntimeException  If any errors happen, the exception will be logged automatically
   */
  protected abstract void toNetworkSafe(FriendlyByteBuf buffer, T recipe);

  @Nullable
  @Override
  public T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
    try {
      return fromNetworkSafe(id, buffer);
    } catch (RuntimeException e) {
      Mantle.logger.error("{}: Error writing recipe to packet", this.getClass().getSimpleName(), e);
      throw e;
    }
  }

  @Override
  public void toNetwork(FriendlyByteBuf buffer, T recipe) {
    try {
      toNetworkSafe(buffer, recipe);
    } catch (RuntimeException e) {
      Mantle.logger.error("{}: Error reading recipe from packet", this.getClass().getSimpleName(), e);
      throw e;
    }
  }
}

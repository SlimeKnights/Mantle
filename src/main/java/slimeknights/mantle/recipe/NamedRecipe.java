package slimeknights.mantle.recipe;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** Interface for a pair of recipe and name. Like {@link net.minecraft.world.item.crafting.RecipeHolder} but without the strict bound. */
public interface NamedRecipe<T> {
  /** Gets the ID for the recipe. */
  ResourceLocation id();

  /** Gets a list of source IDs for a dynamic recipe. */
  default List<ResourceLocation> sourceIds() {
    return List.of(id());
  }

  /** Gets the recipe instance */
  T recipe();

  /** Named recipe that is itself the recipe */
  interface Self<T> extends NamedRecipe<T> {
    @Override
    @SuppressWarnings("unchecked")
    default T recipe() {
      return (T) this;
    }
  }


  /** Creates a new named recipe instance */
  static <T> NamedRecipe<T> of(ResourceLocation id, T recipe) {
    return new Wrapped<>(id, recipe);
  }

  /** Creates a new named recipe instance from the given recipe holder. */
  static <T extends Recipe<?>> NamedRecipe<T> of(RecipeHolder<T> holder) {
    return new Wrapped<>(holder.id(), holder.value());
  }

  /** Creates a new named recipe instance with the given list of parent IDs. */
  static <T> NamedRecipe<T> of(ResourceLocation id, List<ResourceLocation> parents, T recipe) {
    return new Wrapped<>(id, parents, recipe);
  }

  /** Gets a recipe class for JEI with the given type. */
  @SuppressWarnings({"unchecked", "RedundantCast"})
  static <T> Class<? extends NamedRecipe<T>> recipeClass() {
    return (Class<? extends NamedRecipe<T>>)(Class<?>) NamedRecipe.class;
  }

  /** Creates a stream codec for the given recipe codec. */
  static <B extends ByteBuf,R> StreamCodec<B,NamedRecipe<R>> streamCodec(StreamCodec<B,R> codec) {
    return StreamCodec.composite(ResourceLocation.STREAM_CODEC, NamedRecipe::id, codec, NamedRecipe::recipe, NamedRecipe::of);
  }

  /** Wrapper around an ID and recipe pair. */
  record Wrapped<T>(ResourceLocation id, List<ResourceLocation> sourceIds, T recipe) implements NamedRecipe<T> {
    /** Creates a wrapped recipe using the recipe ID as the parent ID. */
    public Wrapped(ResourceLocation id, T recipe) {
      this(id, List.of(id), recipe);
    }
  }
}

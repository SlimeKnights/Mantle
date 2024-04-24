package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Recipe serializer instance using loadables. Use {@link ContextKey#ID} to get the recipe ID.
 * @param <T>  Recipe type
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoadableRecipeSerializer<T extends Recipe<?>> implements LoggingRecipeSerializer<T> {
  /** Context key to use if you want the recipe serializer passed into your recipe */
  public static final ContextKey<RecipeSerializer<?>> SERIALIZER = new ContextKey<>("serializer");
  /** Context key to use if you want a type aware serializer in the recipe, requires {@link #of(RecordLoadable, Supplier)} for your serializer. */
  public static final ContextKey<TypeAwareRecipeSerializer<?>> TYPED_SERIALIZER = new ContextKey<>("typed_serializer");
  /** Context key to use if you want the recipe type passed into your recipe, requires {@link #of(RecordLoadable, Supplier)} for your serializer. */
  public static final ContextKey<RecipeType<?>> TYPE = new ContextKey<>("type");
  /** Field for a group key in a recipe (common requirement) */
  public static final LoadableField<String,Recipe<?>> RECIPE_GROUP = StringLoadable.DEFAULT.defaultField("group", "", Recipe::getGroup);


  protected final RecordLoadable<T> loadable;

  /** Creates a standard serializer from a loadable */
  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return new LoadableRecipeSerializer<>(loadable);
  }

  /** Creates a type aware serializer from a loadable */
  public static <T extends R, R extends Recipe<?>> TypeAwareRecipeSerializer<T> of(RecordLoadable<T> loadable, Supplier<? extends RecipeType<R>> type) {
    return new TypeAware<>(loadable, type);
  }

  /** Builds a context for the given ID */
  protected TypedMapBuilder buildContext(ResourceLocation id) {
    return TypedMapBuilder.builder().put(ContextKey.ID, id).put(SERIALIZER, this);
  }

  @Override
  public T fromJson(ResourceLocation id, JsonObject json) {
    return loadable.deserialize(json, buildContext(id).build());
  }

  @Override
  public T fromNetworkSafe(ResourceLocation id, FriendlyByteBuf buffer) {
    return loadable.decode(buffer, buildContext(id).build());
  }

  @Nullable
  @Override
  public T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
    try {
      return fromNetworkSafe(id, buffer);
    } catch (RuntimeException e) {
      Mantle.logger.error("{}: Error reading recipe {} from packet using loadable {}", this.getClass().getSimpleName(), id, loadable, e);
      throw e;
    }
  }

  @Override
  public void toNetworkSafe(FriendlyByteBuf buffer, T recipe) {
    loadable.encode(buffer, recipe);
  }

  public static class TypeAware<T extends Recipe<?>> extends LoadableRecipeSerializer<T> implements TypeAwareRecipeSerializer<T> {
    private final Supplier<? extends RecipeType<?>> type;
    protected TypeAware(RecordLoadable<T> loadable, Supplier<? extends RecipeType<?>> type) {
      super(loadable);
      this.type = type;
    }

    @Override
    protected TypedMapBuilder buildContext(ResourceLocation id) {
      return super.buildContext(id).put(TYPE, getType()).put(TYPED_SERIALIZER, this);
    }

    @Override
    public RecipeType<?> getType() {
      return type.get();
    }

    @Nullable
    @Override
    public T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
      try {
        return fromNetworkSafe(id, buffer);
      } catch (RuntimeException e) {
        Mantle.logger.error("{}: Error reading recipe {} of type {} from packet using loadable {}", this.getClass().getSimpleName(), id, getType(), loadable, e);
        throw e;
      }
    }
  }
}

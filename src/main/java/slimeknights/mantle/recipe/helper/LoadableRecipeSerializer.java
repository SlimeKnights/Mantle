package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.network.codec.StreamableCodec;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Recipe serializer instance using loadables. Use {@link ContextKey#ID} to get the recipe ID.
 * @param <T>  Recipe type
 */
@Getter
@Accessors(fluent = true)
public class LoadableRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T>, Consumer<TypedMapBuilder> {
  /** Context key to use if you want the recipe serializer passed into your recipe */
  public static final ContextKey<RecipeSerializer<?>> SERIALIZER = new ContextKey<>("serializer");
  /** Context key to use if you want the recipe type passed into your recipe, requires {@link #of(RecordLoadable, Supplier)} for your serializer. */
  public static final ContextKey<RecipeType<?>> TYPE = new ContextKey<>("type");
  /** Field for a group key in a recipe (common requirement) */
  public static final LoadableField<String,Recipe<?>> RECIPE_GROUP = StringLoadable.DEFAULT.defaultField("group", "", Recipe::getGroup);

  private final MapCodec<T> codec;
  private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;

  protected LoadableRecipeSerializer(RecordLoadable<T> loadable) {
    this.codec = new LoadableMapCodec<>(loadable, this);
    this.streamCodec = new StreamableCodec<>(loadable, this);
  }

  /** Applies the serializer to the context */
  @Override
  public void accept(TypedMapBuilder builder) {
    builder.put(SERIALIZER, this);
  }


  /* Static constructors */

  /** Creates a standard serializer from a loadable */
  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return new LoadableRecipeSerializer<>(loadable);
  }

  /** Creates a type aware serializer from a loadable */
  public static <T extends R, R extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable, Supplier<? extends RecipeType<R>> type) {
    return new TypeAware<>(loadable, type);
  }

  /** Recipe serializer that passes the recipe type to the recipe constructor */
  private static class TypeAware<T extends Recipe<?>> extends LoadableRecipeSerializer<T> {
    private final Supplier<? extends RecipeType<?>> type;
    protected TypeAware(RecordLoadable<T> loadable, Supplier<? extends RecipeType<?>> type) {
      super(loadable);
      this.type = type;
    }

    @Override
    public void accept(TypedMapBuilder builder) {
      super.accept(builder);
      builder.put(TYPE, type.get());
    }
  }
}

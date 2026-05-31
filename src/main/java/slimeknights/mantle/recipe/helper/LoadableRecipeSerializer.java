package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Recipe serializer instance using loadables. Use {@link ContextKey#ID} to get the recipe ID.
 * @param <T>  Recipe type
 */
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
  private final MapCodec<T> codec;
  private final StreamCodec<RegistryFriendlyByteBuf,T> streamCodec;

  protected LoadableRecipeSerializer(RecordLoadable<T> loadable) {
    this.loadable = loadable;
    this.codec = MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH.xmap(dynamic -> {
      JsonObject json = dynamic.convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
      return loadable.deserialize(json, buildContext(null).build());
    }, object -> new Dynamic<>(JsonOps.INSTANCE, loadable.serialize(object))));
    this.streamCodec = StreamCodec.of((buffer, recipe) -> {
      buffer.writeResourceLocation(getRecipeId(recipe));
      toNetworkSafe(buffer, recipe);
    }, buffer -> fromNetworkSafe(buffer.readResourceLocation(), buffer));
  }

  /** Creates a standard serializer from a loadable */
  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return new LoadableRecipeSerializer<>(loadable);
  }

  /** Creates a type aware serializer from a loadable */
  public static <T extends R, R extends Recipe<?>> TypeAwareRecipeSerializer<T> of(RecordLoadable<T> loadable, Supplier<? extends RecipeType<R>> type) {
    return new TypeAware<>(loadable, type);
  }

  /** Creates a serializer that is deprecated, logging a warning when used */
  public static <T extends Recipe<?>> RecipeSerializer<T> deprecated(RecordLoadable<T> loadable, String replacement) {
    return new Deprecated<>(loadable, replacement);
  }

  /** Builds a context for the given ID */
  protected TypedMapBuilder buildContext(@Nullable ResourceLocation id) {
    TypedMapBuilder builder = TypedMapBuilder.builder().put(ContextKey.DEBUG, id == null ? "Recipe" : "Recipe " + id).put(SERIALIZER, this);
    if (id != null) {
      builder.put(ContextKey.ID, id);
    }
    return builder;
  }

  public T fromJson(ResourceLocation id, JsonObject json) {
    return loadable.deserialize(json, buildContext(id).build());
  }

  public T fromNetworkSafe(@Nullable ResourceLocation id, FriendlyByteBuf buffer) {
    return loadable.decode(buffer, buildContext(id).build());
  }

  @Nullable
  public T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
    try {
      return fromNetworkSafe(id, buffer);
    } catch (RuntimeException e) {
      Mantle.logger.error("{}: Error reading recipe {} from packet using loadable {}", this.getClass().getSimpleName(), id, loadable, e);
      throw e;
    }
  }

  public void toNetworkSafe(FriendlyByteBuf buffer, T recipe) {
    try {
      loadable.encode(buffer, recipe);
    } catch (RuntimeException e) {
      ResourceLocation id = getRecipeId(recipe);
      Mantle.logger.error("{}: Error writing recipe {} to packet using loadable {}", this.getClass().getSimpleName(), id, loadable, e);
      throw e;
    }
  }

  /** Gets the recipe ID from legacy recipe classes for 1.21 recipe packets. */
  private static ResourceLocation getRecipeId(Recipe<?> recipe) {
    try {
      Method method = recipe.getClass().getMethod("getId");
      if (method.invoke(recipe) instanceof ResourceLocation id) {
        return id;
      }
    } catch (ReflectiveOperationException e) {
      return Mantle.getResource("unknown_loadable_recipe");
    }
    return Mantle.getResource("unknown_loadable_recipe");
  }

  @Override
  public MapCodec<T> codec() {
    return codec;
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf,T> streamCodec() {
    return streamCodec;
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
    public T fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
      try {
        return fromNetworkSafe(id, buffer);
      } catch (RuntimeException e) {
        Mantle.logger.error("{}: Error reading recipe {} of type {} from packet using loadable {}", this.getClass().getSimpleName(), id, getType(), loadable, e);
        throw e;
      }
    }
  }

  /** Helper class that logs a warning on recipe parse about planned removal */
  private static class Deprecated<T extends Recipe<?>> extends LoadableRecipeSerializer<T> {
    private final String replacement;
    protected Deprecated(RecordLoadable<T> loadable, String replacement) {
      super(loadable);
      this.replacement = replacement;
    }

    @Override
    public T fromJson(ResourceLocation id, JsonObject json) {
      T recipe = super.fromJson(id, json);
      Mantle.logger.warn("Using deprecated recipe serializer {}, {}", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this), replacement);
      return recipe;
    }
  }
}

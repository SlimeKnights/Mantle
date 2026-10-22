package slimeknights.mantle.registration.deferred;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.SimpleRecipeSerializer;
import slimeknights.mantle.registration.holder.DeferredRecipeSerializer;

import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for {@link RecipeSerializer}. Provides helpers for using {@link SimpleRecipeSerializer} and {@link LoadableRecipeSerializer}. */
public class RecipeSerializerDeferredRegister extends DeferredRegister<RecipeSerializer<?>> {
  public RecipeSerializerDeferredRegister(String namespace) {
    super(Registries.RECIPE_SERIALIZER, namespace);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends RecipeSerializer<?>> DeferredHolder<RecipeSerializer<?>, I> createHolder(ResourceKey<? extends Registry<RecipeSerializer<?>>> registryKey, ResourceLocation key) {
    // safe as long as no one uses the direct registration methods
    return (DeferredHolder<RecipeSerializer<?>, I>) DeferredRecipeSerializer.createSerializer(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #registerSerializer(String, Function)} */
  @Deprecated
  @Override
  public <I extends RecipeSerializer<?>> DeferredHolder<RecipeSerializer<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /** @deprecated use {@link #registerSerializer(String, Supplier)} */
  @Deprecated
  @Override
  public <I extends RecipeSerializer<?>> DeferredHolder<RecipeSerializer<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** Registers a recipe serializer sensitive to the ID. */
  public <T extends Recipe<?>> DeferredRecipeSerializer<T> registerSerializer(String name, Function<ResourceLocation,RecipeSerializer<T>> constructor) {
    return (DeferredRecipeSerializer<T>) register(name, constructor);
  }

  /** Registers a recipe serializer sensitive ignoring the ID. */
  public <T extends Recipe<?>> DeferredRecipeSerializer<T> registerSerializer(String name, Supplier<RecipeSerializer<T>> constructor) {
    return (DeferredRecipeSerializer<T>) register(name, constructor);
  }

  /** Registers a {@link SimpleRecipeSerializer} from the given codecs. */
  public <T extends Recipe<?>> DeferredRecipeSerializer<T> registerSimple(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf,T> streamCodec) {
    return registerSerializer(name, id -> new SimpleRecipeSerializer<>(codec, streamCodec));
  }

  /** Registers a {@link SimpleRecipeSerializer} from the given loadable. */
  public <T extends Recipe<?>> DeferredRecipeSerializer<T> registerLoadable(String name, RecordLoadable<T> loader) {
    return registerSerializer(name, id -> LoadableRecipeSerializer.of(loader));
  }

  /** Registers a {@link SimpleRecipeSerializer} from the given loadable. */
  public <T extends R, R extends Recipe<?>> DeferredRecipeSerializer<T> registerLoadable(String name, RecordLoadable<T> loader, Supplier<? extends RecipeType<R>> type) {
    return registerSerializer(name, id -> LoadableRecipeSerializer.of(loader, type));
  }
}

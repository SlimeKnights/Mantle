package slimeknights.mantle.registration.deferred;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.network.codec.StreamableCodec;
import slimeknights.mantle.registration.holder.DeferredIngredient;

import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for {@link IngredientType}. Provides non-supplier registration methods using codecs. */
public class IngredientDeferredRegister extends DeferredRegister<IngredientType<?>> {
  public IngredientDeferredRegister(String namespace) {
    super(NeoForgeRegistries.Keys.INGREDIENT_TYPES, namespace);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends IngredientType<?>> DeferredHolder<IngredientType<?>, I> createHolder(ResourceKey<? extends Registry<IngredientType<?>>> registryKey, ResourceLocation key) {
    // this should be fine as IngredientType is a record
    return (DeferredHolder<IngredientType<?>, I>) DeferredIngredient.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #register(String, MapCodec, StreamCodec)} */
  @Deprecated
  @Override
  public <I extends IngredientType<?>> DeferredHolder<IngredientType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /** @deprecated use {@link #register(String, MapCodec, StreamCodec)} */
  @Deprecated
  @Override
  public <I extends IngredientType<?>> DeferredHolder<IngredientType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** Registers an ingredient from the given codecs */
  public <T extends ICustomIngredient> DeferredIngredient<T> register(String name, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) {
    return (DeferredIngredient<T>) register(name, () -> new IngredientType<>(codec, streamCodec));
  }

  /** Registers an ingredient from the given codec. Generally better to use {@link #register(String, MapCodec, StreamCodec)}. */
  public <T extends ICustomIngredient> DeferredIngredient<T> register(String name, MapCodec<T> codec) {
    return (DeferredIngredient<T>) register(name, () -> new IngredientType<>(codec));
  }

  /** Registers an ingredient from the given loadable */
  public <T extends ICustomIngredient> DeferredIngredient<T> register(String name, RecordLoadable<T> loadable) {
    return register(name, new LoadableMapCodec<>(loadable), new StreamableCodec<>(loadable));
  }
}

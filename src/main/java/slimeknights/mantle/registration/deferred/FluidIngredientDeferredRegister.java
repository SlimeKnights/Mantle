package slimeknights.mantle.registration.deferred;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.data.codec.LoadableMapCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.network.codec.StreamableCodec;
import slimeknights.mantle.registration.holder.DeferredFluidIngredient;

import java.util.function.Function;
import java.util.function.Supplier;

/** Deferred register extension for registering ingredient types. */
public class FluidIngredientDeferredRegister extends DeferredRegister<FluidIngredientType<?>> {
  public FluidIngredientDeferredRegister(String namespace) {
    super(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES, namespace);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends FluidIngredientType<?>> DeferredHolder<FluidIngredientType<?>, I> createHolder(ResourceKey<? extends Registry<FluidIngredientType<?>>> registryKey, ResourceLocation key) {
    // this should be fine as FluidIngredientType is a record
    return (DeferredHolder<FluidIngredientType<?>, I>) DeferredFluidIngredient.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #register(String, MapCodec, StreamCodec)} */
  @Deprecated
  @Override
  public <I extends FluidIngredientType<?>> DeferredHolder<FluidIngredientType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /** @deprecated use {@link #register(String, MapCodec, StreamCodec)} */
  @Deprecated
  @Override
  public <I extends FluidIngredientType<?>> DeferredHolder<FluidIngredientType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** Registers a fluid ingredient from the given codecs */
  public <T extends FluidIngredient> DeferredFluidIngredient<T> register(String name, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) {
    return (DeferredFluidIngredient<T>) register(name, () -> new FluidIngredientType<>(codec, streamCodec));
  }

  /** Registers a fluid ingredient from the given codec. Generally better to use {@link #register(String, MapCodec, StreamCodec)}. */
  public <T extends FluidIngredient> DeferredFluidIngredient<T> register(String name, MapCodec<T> codec) {
    return (DeferredFluidIngredient<T>) register(name, () -> new FluidIngredientType<>(codec));
  }

  /** Registers a fluid ingredient from the given loadable */
  public <T extends FluidIngredient> DeferredFluidIngredient<T> register(String name, RecordLoadable<T> loadable) {
    return register(name, new LoadableMapCodec<>(loadable), new StreamableCodec<>(loadable));
  }
}

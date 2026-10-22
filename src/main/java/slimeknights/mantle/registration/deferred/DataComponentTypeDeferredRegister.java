package slimeknights.mantle.registration.deferred;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.holder.DeferredDataComponent;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/** {@link DeferredRegister} for {@link DataComponentType}. Has non-supplier methods for codecs. */
public class DataComponentTypeDeferredRegister extends DeferredRegister<DataComponentType<?>> {
  public DataComponentTypeDeferredRegister(ResourceKey<? extends Registry<DataComponentType<?>>> registryKey, String namespace) {
    super(registryKey, namespace);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends DataComponentType<?>> DeferredHolder<DataComponentType<?>, I> createHolder(ResourceKey<? extends Registry<DataComponentType<?>>> registryKey, ResourceLocation key) {
    // this should be fine as long as no one uses the raw register with a custom data component type implementation
    return (DeferredHolder<DataComponentType<?>, I>) DeferredDataComponent.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #registerType(String, UnaryOperator)} or one of the dedicated helpers. */
  @Deprecated
  @Override
  public <I extends DataComponentType<?>> DeferredHolder<DataComponentType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** @deprecated use {@link #registerType(String, UnaryOperator)} or one of the dedicated helpers. */
  @Deprecated
  @Override
  public <I extends DataComponentType<?>> DeferredHolder<DataComponentType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /** Registers a data component type using the given consumer to fill the builder */
  public <T> DeferredDataComponent<T> registerType(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
    return (DeferredDataComponent<T>) register(name, id -> builder.apply(DataComponentType.builder()).build());
  }

  /** Registers a data component type that is serialized to the data component holder on save. */
  public <T> DeferredDataComponent<T> registerPersistent(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) {
    return registerType(name, builder -> builder.persistent(codec).networkSynchronized(streamCodec));
  }

  /** Registers a data component type that is stored on the data component holder but not saved. */
  public <T> DeferredDataComponent<T> registerVolatile(String name, StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) {
    return registerType(name, builder -> builder.networkSynchronized(streamCodec));
  }
}

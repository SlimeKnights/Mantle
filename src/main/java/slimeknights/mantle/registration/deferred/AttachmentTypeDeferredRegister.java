package slimeknights.mantle.registration.deferred;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.registration.holder.DeferredAttachment;

import java.util.function.Function;
import java.util.function.Supplier;

/** {@link DeferredRegister} for {@link AttachmentType}. Has a helper to process the builder. */
public class AttachmentTypeDeferredRegister extends DeferredRegister<AttachmentType<?>> {
  public AttachmentTypeDeferredRegister(String namespace) {
    super(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, namespace);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected <I extends AttachmentType<?>> DeferredHolder<AttachmentType<?>, I> createHolder(ResourceKey<? extends Registry<AttachmentType<?>>> registryKey, ResourceLocation key) {
    // should be fine as AttachmentType is final
    return (DeferredHolder<AttachmentType<?>, I>) DeferredAttachment.createType(ResourceKey.create(registryKey, key));
  }

  /** @deprecated use {@link #registerType(String, Supplier)} or one of the specialied helpers */
  @Deprecated
  @Override
  public <I extends AttachmentType<?>> DeferredHolder<AttachmentType<?>, I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return super.register(name, func);
  }

  /** @deprecated use {@link #registerType(String, Supplier)} or one of the specialied helpers */
  @Deprecated
  @Override
  public <I extends AttachmentType<?>> DeferredHolder<AttachmentType<?>, I> register(String name, Supplier<? extends I> sup) {
    return super.register(name, sup);
  }

  /** Registers an attachment type using the given consumer to fill the builder */
  public <T> DeferredAttachment<T> registerType(String name, Supplier<AttachmentType.Builder<T>> supplier) {
    return (DeferredAttachment<T>) register(name, id -> supplier.get().build());
  }
}

package slimeknights.mantle.registration.holder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** {@link DeferredHolder} for {@link AttachmentType}. */
public class DeferredAttachment<T> extends DeferredHolder<AttachmentType<?>, AttachmentType<T>> {
  protected DeferredAttachment(ResourceKey<AttachmentType<?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T> DeferredAttachment<T> createType(ResourceLocation key) {
    return createType(ResourceKey.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T> DeferredAttachment<T> createType(ResourceKey<AttachmentType<?>> key) {
    return new DeferredAttachment<>(key);
  }
}

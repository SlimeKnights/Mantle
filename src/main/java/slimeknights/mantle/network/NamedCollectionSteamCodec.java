package slimeknights.mantle.network;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.registration.object.IdAwareObject;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Helper for creating packets using {@link Streamable} with objects that know their own ID but receive it via {@link TypedMap} context. */
public interface NamedCollectionSteamCodec<T extends IdAwareObject> extends StreamCodec<RegistryFriendlyByteBuf, Collection<T>> {
  /** Gets the name to use in any errors related to this packet. */
  String debugName();

  /** Gets the streamable instance for reading and writing the object. */
  Streamable<T> streamable();

  /** Gets the context for the given ID. By default, includes  */
  default TypedMap makeContext(ResourceLocation id, RegistryAccess access) {
    return TypedMapBuilder.builder()
      .put(ContextKey.ID, id)
      .put(ContextKey.DEBUG, debugName() + ' ' + id)
      .put(ContextKey.REGISTRY_LOOKUP, access)
      .build();
  }

  @Override
  default void encode(RegistryFriendlyByteBuf buffer, Collection<T> collection) {
    Streamable<T> streamable = streamable();
    buffer.writeVarInt(collection.size());
    for (T value : collection) {
      buffer.writeResourceLocation(value.getId());
      // add more context to error message and ensure its logged
      try {
        streamable.encode(buffer, value);
      } catch (RuntimeException e) {
        Mantle.logger.error("Failed to encode {} with ID {}", debugName(), value.getId(), e);
        // can't recover as packet has too little data for what it said
        throw e;
      }
    }
  }

  @Override
  default Collection<T> decode(RegistryFriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    RegistryAccess access = buffer.registryAccess();
    List<T> collection = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ResourceLocation id = buffer.readResourceLocation();
      // add more context to error message and ensure its logged
      try {
        collection.add(streamable().decode(buffer, makeContext(id, access)));
      } catch (RuntimeException e) {
        Mantle.logger.error("Failed to decode {} with ID {}", debugName(), id, e);
        // if in production, attempt to recover by returning what worked
        if (FMLEnvironment.production) {
          break;
        } else {
          // in dev throw as people should notice to fix their mods
          throw e;
        }
      }
    }
    return collection;
  }
}

package slimeknights.mantle.data.loadable.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.typed.TypedMap;

/** Simple loadable mapping GSON to loadable. Uses NBT for networking */
@SuppressWarnings("unused")  // API
public record GsonLoadable<T>(Gson gson, Class<T> classType) implements Loadable<T> {
  @Override
  public T convert(JsonElement json, String s, TypedMap context) {
    return gson.fromJson(json, classType);
  }

  @Override
  public JsonElement serialize(T object, TypedMap context) {
    return gson.toJsonTree(object, classType);
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    Tag tag = buffer.readNbt(NbtAccounter.create(2097152L));
    if (tag != null) {
      return gson.fromJson(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag), classType);
    }
    throw new DecoderException("Failed to decode: " + classType.getSimpleName());
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, T object, TypedMap context) {
    buffer.writeNbt(JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, gson.toJsonTree(object, classType)));
  }
}

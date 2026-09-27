package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

/** Implementation of a loadable using a codec. Note this will be inefficient when reading from and writing to the network */
public record CodecLoadable<T>(DynamicOps<Tag> ops, Codec<T> codec) implements Loadable<T> {
  public CodecLoadable(Codec<T> codec) {
    this(NbtOps.INSTANCE, codec);
  }

  @Override
  public T convert(JsonElement element, String key, TypedMap context) {
    return JsonHelper.parse(codec, element);
  }

  @Override
  public JsonElement serialize(T object) {
    return JsonHelper.serialize(codec, object);
  }

  @Override
  public T decode(FriendlyByteBuf buffer, TypedMap context) {
    return buffer.readWithCodecTrusted(ops, codec);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, T object) {
    buffer.writeWithCodec(ops, codec, object);
  }
}

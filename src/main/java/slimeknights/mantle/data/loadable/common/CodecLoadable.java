package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;

/** Implementation of a loadable using a codec. Note this will be inefficient when reading from and writing to the network */
public interface CodecLoadable<T> extends Loadable<T> {
  /** Codec used for JSON. */
  Codec<T> codec();

  /** Stream codec used for the buffer. If {@code null}, uses {@link #codec()} instead. */
  @Nullable
  StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

  @Override
  default JsonElement serialize(T object, TypedMap context) {
    return JsonHelper.serialize(codec(), object);
  }

  @Override
  default T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    StreamCodec<? super RegistryFriendlyByteBuf, T> codec = streamCodec();
    if (codec == null) {
      throw new IllegalStateException("The codec " + codec() + " does not support decoding from network.");
    }
    return codec.decode(buffer);
  }

  @Override
  default void encode(RegistryFriendlyByteBuf buffer, T object, TypedMap context) {
    StreamCodec<? super RegistryFriendlyByteBuf, T> codec = streamCodec();
    if (codec == null) {
      throw new IllegalStateException("The codec " + codec() + " does not support encoding to network.");
    }
    codec.encode(buffer, object);
  }

  /** Parses the value directly without registry access */
  @SuppressWarnings("unused")  // API
  record Direct<T>(Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) implements CodecLoadable<T> {
    public Direct(Codec<T> codec) {
      this(codec, ByteBufCodecs.fromCodec(codec));
    }

    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      return JsonHelper.parse(codec, element);
    }
  }

  /** Uses registry access to parse the object. */
  record Registry<T>(Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) implements CodecLoadable<T> {
    public Registry(Codec<T> codec) {
      this(codec, ByteBufCodecs.fromCodecWithRegistries(codec));
    }

    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      return JsonHelper.parse(ContextKey.createSerializationContext(context, ErrorFactory.JSON_SYNTAX_ERROR), codec, element);
    }

    @Override
    public JsonElement serialize(T object, TypedMap context) {
      return JsonHelper.serialize(ContextKey.createSerializationContext(context, ErrorFactory.RUNTIME), codec, object);
    }
  }
}

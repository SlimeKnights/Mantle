package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
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

  /** Gets the ops to use for the buffer when writing using the codec. */
  default DynamicOps<Tag> bufferOps(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return NbtOps.INSTANCE;
  }

  @SuppressWarnings("deprecation")  // if its removed we will just throw instead
  @Override
  default T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    StreamCodec<? super RegistryFriendlyByteBuf, T> stream = streamCodec();
    if (stream != null) {
      return stream.decode(buffer);
    } else {
      return buffer.readWithCodecTrusted(bufferOps(buffer, context), codec());
    }
  }

  @SuppressWarnings("deprecation")  // if its removed we will just throw instead
  @Override
  default void encode(RegistryFriendlyByteBuf buffer, T object, TypedMap context) {
    StreamCodec<? super RegistryFriendlyByteBuf, T> stream = streamCodec();
    if (stream != null) {
      stream.encode(buffer, object);
    } else {
      buffer.writeWithCodec(bufferOps(buffer, context), codec(), object);
    }
  }

  /** Parses the value directly without registry access */
  record Direct<T>(Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) implements CodecLoadable<T> {
    public Direct(Codec<T> codec) {
      this(codec, null);
    }

    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      return JsonHelper.parse(codec, element);
    }
  }

  /** Uses registry access to parse the object. */
  record Registry<T>(Codec<T> codec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) implements CodecLoadable<T> {
    public Registry(Codec<T> codec) {
      this(codec, null);
    }

    @Override
    public T convert(JsonElement element, String key, TypedMap context) {
      return JsonHelper.parse(ContextKey.createSerializationContext(context, ErrorFactory.JSON_SYNTAX_ERROR), codec, element);
    }

    @Override
    public JsonElement serialize(T object, TypedMap context) {
      return JsonHelper.serialize(ContextKey.createSerializationContext(context, ErrorFactory.RUNTIME), codec, object);
    }

    @Override
    public DynamicOps<Tag> bufferOps(RegistryFriendlyByteBuf buffer, TypedMap context) {
      // use registry ops
      RegistryOps<Tag> registryOps = buffer.registryAccess().createSerializationContext(NbtOps.INSTANCE);
      // include condition context if present
      IContext conditionContext = context.get(ContextKey.CONDITION_CONTEXT);
      if (conditionContext != null) {
        return new ConditionalOps<>(registryOps, conditionContext);
      }
      return registryOps;
    }
  }
}

package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

/** Record field that always returns the same value, used mainly to pass a different object in JSON vs buffer parsing */
public record ConstantField<T>(T fromJson, T fromBuffer) implements LoadableField<T,Object> {
  public ConstantField(T value) {
    this(value, value);
  }

  @Override
  public T get(JsonObject json) {
    return fromJson;
  }

  @Override
  public T decode(FriendlyByteBuf buffer) {
    return fromBuffer;
  }

  @Override
  public void serialize(Object parent, JsonObject json) {}

  @Override
  public void encode(FriendlyByteBuf buffer, Object parent) {}
}

package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.util.typed.TypedMap;

/** Record field that always returns the same value, used mainly to pass a different object in JSON vs buffer parsing */
public record ConstantField<T>(T fromJson, T fromBuffer) implements RecordField<T,Object> {
  public ConstantField(T value) {
    this(value, value);
  }

  @Override
  public T get(JsonObject json, TypedMap context) {
    return fromJson;
  }

  @Override
  public T decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return fromBuffer;
  }

  @Override
  public void serialize(Object parent, JsonObject json) {}

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, Object parent) {}
}

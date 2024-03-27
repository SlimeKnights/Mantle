package slimeknights.mantle.data.loadable.primitive;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.Locale;

/** Loadable for an enum value */
public record EnumLoadable<E extends Enum<E>>(Class<E> enumClass, E... allowedValues) implements Loadable<E> {
  public EnumLoadable(Class<E> enumClass) {
    this(enumClass, enumClass.getEnumConstants());
  }

  @Override
  public E convert(JsonElement element, String key) {
    String name = GsonHelper.convertToString(element, key);
    for (E value : allowedValues) {
      if (value.name().toLowerCase(Locale.ROOT).equals(name)) {
        return value;
      }
    }
    throw new JsonSyntaxException("Invalid " + enumClass.getSimpleName() + " " + name);
  }

  @Override
  public JsonElement serialize(E object) {
    return new JsonPrimitive(object.name().toLowerCase(Locale.ROOT));
  }

  @Override
  public E decode(FriendlyByteBuf buffer) {
    return buffer.readEnum(enumClass);
  }

  @Override
  public void encode(FriendlyByteBuf buffer, E object) {
    buffer.writeEnum(object);
  }
}

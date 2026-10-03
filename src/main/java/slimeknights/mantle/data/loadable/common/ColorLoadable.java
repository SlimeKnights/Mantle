package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/** Loadable to fetch colors from JSON */
@RequiredArgsConstructor
public enum ColorLoadable implements StringLoadable<Integer> {
  ALPHA {
    @Override
    public Integer parseString(String color, String key, TypedMap context) {
      // two options, 6 character or 8 character, must not start with - sign
      if (color.charAt(0) != '-') {
        try {
          // length of 8 must parse as long, supports transparency
          int length = color.length();
          if (length == 8) {
            return (int)Long.parseLong(color, 16);
          }
          if (length == 6) {
            return 0xFF000000 | Integer.parseInt(color, 16);
          }
        } catch (NumberFormatException ex) {
          // NO-OP
        }
      }
      throw new JsonSyntaxException("Invalid color '" + color + "' at " + key);
    }

    @Override
    public String getString(Integer color) {
      return String.format("%08X", color);
    }
  },
  NO_ALPHA {
    @Override
    public Integer parseString(String color, String key, TypedMap context) {
      // only consider 6 digits with no alpha, will force to full alpha
      if (color.charAt(0) != '-' && color.length() == 6) {
        try {
          return 0xFF000000 | Integer.parseInt(color, 16);
        } catch (NumberFormatException ex) {
          // NO-OP
        }
      }
      throw new JsonSyntaxException("Invalid color '" + color + "' at " + key);
    }

    @Override
    public String getString(Integer color) {
      return String.format("%06X", color & 0xFFFFFF);
    }
  };

  @Override
  public Integer decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return buffer.readInt();
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, Integer color) {
    buffer.writeInt(color);
  }

  /** Fetches the color from the parent, defaulting to white if missing */
  public int getOrWhite(JsonObject parent, String key) {
    return getOrDefault(parent, key, -1);
  }

  /** Creates a field that defaults to white */
  public <P> LoadableField<Integer, P> defaultField(String key, boolean serializeDefault, Function<P, Integer> getter) {
    return StringLoadable.super.defaultField(key, -1, serializeDefault, getter);
  }
}

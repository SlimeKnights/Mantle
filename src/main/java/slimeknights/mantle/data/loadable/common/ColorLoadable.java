package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;

/** Loadable to fetch colors from JSON */
@RequiredArgsConstructor
public enum ColorLoadable implements Loadable<Integer> {
  ALPHA {
    @Override
    public int parseColor(String color) {
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
      throw new JsonSyntaxException("Invalid color '" + color + "'");
    }

    @Override
    public String colorString(int color) {
      return String.format("%08X", color);
    }
  },
  NO_ALPHA {
    @Override
    public int parseColor(String color) {
      // only consider 6 digits with no alpha, will force to full alpha
      if (color.charAt(0) != '-' && color.length() == 6) {
        try {
          return 0xFF000000 | Integer.parseInt(color, 16);
        } catch (NumberFormatException ex) {
          // NO-OP
        }
      }
      throw new JsonSyntaxException("Invalid color '" + color + "'");
    }

    @Override
    public String colorString(int color) {
      return String.format("%06X", color & 0xFFFFFF);
    }
  };

  /**
   * Parses the color from the given string
   * @param color  Color string
   * @return  Color value
   */
  public abstract int parseColor(String color);

  /** Writes the given color as a string */
  public abstract String colorString(int color);

  @Override
  public Integer convert(JsonElement element, String key) throws JsonSyntaxException {
    return parseColor(GsonHelper.convertToString(element, key));
  }

  @Override
  public JsonElement serialize(Integer color) throws RuntimeException {
    return new JsonPrimitive(colorString(color));
  }

  @Override
  public Integer fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
    return buffer.readInt();
  }

  @Override
  public void toNetwork(Integer color, FriendlyByteBuf buffer) throws EncoderException {
    buffer.writeInt(color);
  }
}

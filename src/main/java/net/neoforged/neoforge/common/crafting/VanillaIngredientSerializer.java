package slimeknights.mantle.compat.neoforged.neoforge.common.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;

public enum VanillaIngredientSerializer implements IIngredientSerializer<Object> {
  INSTANCE;

  @Override
  public Object parse(JsonObject json) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object parse(FriendlyByteBuf buffer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void write(FriendlyByteBuf buffer, Object ingredient) {
    throw new UnsupportedOperationException();
  }
}

package slimeknights.mantle.compat.neoforged.neoforge.common.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class StrictNBTIngredient extends AbstractIngredient {
  protected final ItemStack stack;

  public StrictNBTIngredient(ItemStack stack) {
    super(Stream.of(new ItemValue(stack)));
    this.stack = stack;
  }

  @Override
  public IIngredientSerializer<?> getSerializer() {
    return Serializer.INSTANCE;
  }

  public enum Serializer implements IIngredientSerializer<StrictNBTIngredient> {
    INSTANCE;

    @Override
    public StrictNBTIngredient parse(JsonObject json) {
      throw new UnsupportedOperationException();
    }

    @Override
    public StrictNBTIngredient parse(FriendlyByteBuf buffer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void write(FriendlyByteBuf buffer, StrictNBTIngredient ingredient) {
      throw new UnsupportedOperationException();
    }
  }
}

package slimeknights.mantle.fluid.transfer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.util.JsonHelper;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/** Fluid transfer info that empties a fluid from an item */
@RequiredArgsConstructor
public class EmptyFluidContainerTransfer implements IFluidContainerTransfer.WithDirection {
  public static final ResourceLocation ID = Mantle.getResource("empty_item");

  protected final Ingredient input;
  protected final ItemOutput result;
  protected final FluidOutput fluid;

  /** @deprecated use {@link #EmptyFluidContainerTransfer(Ingredient, ItemOutput, FluidOutput)} */
  @Deprecated(forRemoval = true)
  public EmptyFluidContainerTransfer(Ingredient input, ItemOutput result, FluidStack fluid) {
    this(input, result, FluidOutput.fromStack(fluid));
  }

  @Override
  public void addRepresentativeItems(Consumer<Item> consumer) {
    for (ItemStack stack : input.getItems()) {
      consumer.accept(stack.getItem());
    }
  }

  @Override
  public boolean matches(ItemStack stack, FluidStack fluid) {
    return input.test(stack);
  }

  /** Gets the contained fluid in the given stack */
  protected FluidStack getFluid(ItemStack stack) {
    return fluid.get();
  }

  @Nullable
  @Override
  public TransferResult transfer(ItemStack stack, FluidStack fluid, IFluidHandler handler, TransferDirection direction) {
    if (!direction.canEmpty()) {
      return null;
    }
    FluidStack contained = getFluid(stack);
    int simulated = handler.fill(contained.copy(), FluidAction.SIMULATE);
    if (simulated == contained.getAmount()) {
      int actual = handler.fill(contained.copy(), FluidAction.EXECUTE);
      if (actual > 0) {
        if (actual != this.fluid.getAmount()) {
          Mantle.logger.error("Wrong amount filled from {}, expected {}, filled {}", BuiltInRegistries.ITEM.getKey(stack.getItem()), this.fluid.getAmount(), actual);
        }
        return new TransferResult(result.copy(), contained, false);
      }
    }
    return null;
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    json.addProperty("type", ID.toString());
    json.add("input", input.toJson());
    if (!result.isEmpty()) {
      json.add("result", result.serialize(false));
    }
    json.add("fluid", FluidOutput.Loadable.REQUIRED.serialize(fluid));
    return json;
  }

  /** Unique loader instance */
  public static final JsonDeserializer<EmptyFluidContainerTransfer> DESERIALIZER = new Deserializer<>(EmptyFluidContainerTransfer::new);

  /** Gets the result for the fluid transfer. */
  static ItemOutput getResult(JsonObject json) {
    String key = "result";
    if (!json.has(key) && json.has("filled")) {
      Mantle.logger.warn("Using deprecated field 'filled' for fluid container transfer, use 'result' instead.");
      key = "filled";
    }
    return ItemOutput.Loadable.OPTIONAL_ITEM.getOrEmpty(json, key);
  }

  /**
   * Generic deserializer
   */
  public record Deserializer<T extends EmptyFluidContainerTransfer>(TriFunction<Ingredient,ItemOutput,FluidOutput,T> factory) implements JsonDeserializer<T> {
    @Override
    public T deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      JsonObject json = element.getAsJsonObject();
      Ingredient input = Ingredient.fromJson(JsonHelper.getElement(json, "input"));
      ItemOutput result = getResult(json);
      FluidOutput fluid = FluidOutput.Loadable.REQUIRED.getIfPresent(json, "fluid");
      return factory.apply(input, result, fluid);
    }
  }
}

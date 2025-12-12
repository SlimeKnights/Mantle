package slimeknights.mantle.fluid.transfer;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;

/** Fluid transfer info that empties a fluid from an item, copying the fluid's NBT to the stack */
public class EmptyFluidWithNBTTransfer extends EmptyFluidContainerTransfer {
  public static final ResourceLocation ID = Mantle.getResource("empty_nbt");
  public EmptyFluidWithNBTTransfer(Ingredient input, ItemOutput filled, FluidOutput fluid) {
    super(input, filled, fluid);
  }

  /** @deprecated use {@link #EmptyFluidWithNBTTransfer(Ingredient, ItemOutput, FluidOutput)} */
  @Deprecated(forRemoval = true)
  public EmptyFluidWithNBTTransfer(Ingredient input, ItemOutput filled, FluidStack fluid) {
    this(input, filled, FluidOutput.fromStack(fluid));
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    // TODO: merge NBT?
    return new FluidStack(fluid.get().getFluid(), fluid.getAmount(), stack.getTag());
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = super.serialize(context);
    json.addProperty("type", ID.toString());
    return json;
  }

  /** Unique loader instance */
  public static final JsonDeserializer<EmptyFluidContainerTransfer> DESERIALIZER = new Deserializer<>(EmptyFluidWithNBTTransfer::new);
}

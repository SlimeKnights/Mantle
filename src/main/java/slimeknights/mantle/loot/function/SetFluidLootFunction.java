package slimeknights.mantle.loot.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.recipe.helper.RecipeHelper;

/**
 * Loot function to set the fluid on a dropped item
 */
public class SetFluidLootFunction extends LootItemConditionalFunction {
  public static final Serializer SERIALIZER = new Serializer();

  /** Fluid to add to the item */
  private final FluidStack fluid;
  protected SetFluidLootFunction(LootItemCondition[] conditionsIn, FluidStack fluid) {
    super(conditionsIn);
    this.fluid = fluid;
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> {
                  handler.fill(fluid.copy(), FluidAction.EXECUTE);
                  return handler.getContainer();
                }).orElse(stack);
  }

  @Override
  public LootItemFunctionType getType() {
    return MantleLoot.SET_FLUID_FUNCTION;
  }

  /**
   * Creates a new builder with the given fluid
   * @param fluid  Fluid to set
   * @return  Builder instance
   */
  public static Builder<?> builder(FluidStack fluid) {
    return simpleBuilder(conditions -> new SetFluidLootFunction(conditions, fluid));
  }

  /** Serializer logic for the function */
  private static class Serializer extends LootItemConditionalFunction.Serializer<SetFluidLootFunction> {
    @Override
    public void serialize(JsonObject json, SetFluidLootFunction loot, JsonSerializationContext context) {
      super.serialize(json, loot, context);
      json.add("fluid", RecipeHelper.serializeFluidStack(loot.fluid));
    }

    @Override
    public SetFluidLootFunction deserialize(JsonObject object, JsonDeserializationContext context, LootItemCondition[] conditions) {
      FluidStack fluid = RecipeHelper.deserializeFluidStack(GsonHelper.getAsJsonObject(object, "fluid"));
      return new SetFluidLootFunction(conditions, fluid);
    }
  }
}

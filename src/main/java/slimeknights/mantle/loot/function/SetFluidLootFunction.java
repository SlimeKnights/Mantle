package slimeknights.mantle.loot.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.mantle.loot.MantleLoot;

import java.util.List;

/**
 * Loot function to set the fluid on a dropped item
 */
public class SetFluidLootFunction extends LootItemConditionalFunction {
  public static final MapCodec<SetFluidLootFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
      commonFields(inst).and(
          FluidStack.CODEC.fieldOf("fluid").forGetter(f -> f.fluid)
      ).apply(inst, SetFluidLootFunction::new));

  /** Fluid to add to the item */
  private final FluidStack fluid;

  protected SetFluidLootFunction(List<LootItemCondition> conditions, FluidStack fluid) {
    super(conditions);
    this.fluid = fluid;
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (handler != null) {
      handler.fill(fluid.copy(), FluidAction.EXECUTE);
      return handler.getContainer();
    }
    return stack;
  }

  @Override
  public LootItemFunctionType<SetFluidLootFunction> getType() {
    return MantleLoot.SET_FLUID_FUNCTION.get();
  }

  /**
   * Creates a new builder with the given fluid
   * @param fluid  Fluid to set
   * @return  Builder instance
   */
  public static Builder<?> builder(FluidStack fluid) {
    return simpleBuilder(conditions -> new SetFluidLootFunction(conditions, fluid));
  }
}

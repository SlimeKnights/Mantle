package slimeknights.mantle.loot.function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.material.Fluid;
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
  private static final Codec<FluidStack> LEGACY_FLUID_CODEC = RecordCodecBuilder.create(instance -> instance.group(
    BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidStack::getFluid),
    Codec.INT.fieldOf("amount").forGetter(FluidStack::getAmount)
  ).apply(instance, SetFluidLootFunction::newLegacyFluidStack));
  private static final Codec<FluidStack> FLUID_CODEC = Codec.either(FluidStack.CODEC, LEGACY_FLUID_CODEC)
                                                            .xmap(either -> either.map(stack -> stack, stack -> stack), Either::left);
  public static final MapCodec<SetFluidLootFunction> CODEC = RecordCodecBuilder.mapCodec(
    instance -> commonFields(instance).and(FLUID_CODEC.fieldOf("fluid").forGetter(loot -> loot.fluid))
                            .apply(instance, SetFluidLootFunction::new)
  );

  /** Fluid to add to the item */
  private final FluidStack fluid;
  protected SetFluidLootFunction(List<LootItemCondition> conditionsIn, FluidStack fluid) {
    super(conditionsIn);
    this.fluid = fluid;
  }

  private static FluidStack newLegacyFluidStack(Fluid fluid, int amount) {
    return amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
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
}

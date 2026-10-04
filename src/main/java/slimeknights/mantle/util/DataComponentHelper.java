package slimeknights.mantle.util;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Helpers for working with data components on stacks */
public class DataComponentHelper {
  private DataComponentHelper() {}

  /** Creates an item stack with the given components */
  public static ItemStack makeStack(ItemLike item, int count, DataComponentPatch components) {
    if (item == Items.AIR || count <= 0) {
      return ItemStack.EMPTY;
    }
    ItemStack stack = new ItemStack(item, count);
    if (!components.isEmpty()) {
      stack.applyComponents(components);
    }
    return stack;
  }

  /** Creates an item stack with the given components */
  public static ItemStack makeStack(ItemLike item, DataComponentPatch components) {
    return makeStack(item, 1, components);
  }

  /** Creates a fluid stack with the given components */
  public static FluidStack makeStack(Fluid fluid, int amount, DataComponentPatch components) {
    if (fluid == Fluids.EMPTY || amount <= 0) {
      return FluidStack.EMPTY;
    }
    FluidStack stack = new FluidStack(fluid, amount);
    if (!components.isEmpty()) {
      stack.applyComponents(components);
    }
    return stack;
  }
}

package slimeknights.mantle.util;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

import java.util.List;

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

  /** Copies the given data component to the given stack */
  public static <T, S extends MutableDataComponentHolder> S copy(S to, DataComponentHolder from, DataComponentType<T> type) {
    T value = from.get(type);
    if (value != null) {
      to.set(type, value);
    }
    return to;
  }

  /** Copies the given data components to the given stack */
  @Contract("_,_,_->param1")
  public static <S extends MutableDataComponentHolder> S copy(S to, DataComponentHolder from, List<DataComponentType<?>> types) {
    for (DataComponentType<?> type : types) {
      copy(to, from, type);
    }
    return to;
  }
}

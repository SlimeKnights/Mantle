package slimeknights.mantle.util.sync;

import lombok.AllArgsConstructor;
import net.minecraft.world.inventory.ContainerData;

import java.util.function.Supplier;

/**
 * Int array that wraps an integer array supplier
 */
@AllArgsConstructor
public class IntArrayWrapper implements ContainerData {
  private final Supplier<int[]> sup;

  @Override
  public int get(int index) {
    int[] array = sup.get();
    if (index >= 0 && index < array.length) {
      return array[index];
    }
    return 0;
  }

  @Override
  public void set(int index, int value) {
    int[] array = sup.get();
    if (index >= 0 && index < array.length) {
      array[index] = value;
    }
  }

  @Override
  public int getCount() {
    return sup.get().length;
  }
}

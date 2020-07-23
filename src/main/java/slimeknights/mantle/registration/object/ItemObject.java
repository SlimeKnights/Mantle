package slimeknights.mantle.registration.object;

import lombok.AllArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registry object wrapper to implement {@link IItemProvider}
 * @param <I>  Item class
 */
@SuppressWarnings("unused")
@AllArgsConstructor
public class ItemObject<I extends Item> implements Supplier<I>, IItemProvider {
  private final Supplier<? extends I> item;

  @Override
  public I get() {
    return item.get();
  }

  @Override
  public I asItem() {
    return item.get();
  }

  /**
   * Gets the resource location for the given item
   * @return  Resource location for the given item
   */
  public ResourceLocation getRegistryName() {
    return Objects.requireNonNull(item.get().getRegistryName());
  }
}

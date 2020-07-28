package slimeknights.mantle.registration.object;

import lombok.AllArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.Supplier;

/**
 * Registry object wrapper to implement {@link IItemProvider}
 * @param <I>  Item class
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@AllArgsConstructor
public class ItemObject<I extends IForgeRegistryEntry<? super I> & IItemProvider> implements Supplier<I>, IItemProvider {
  /** Supplier to the registry entry */
  private final Supplier<? extends I> entry;

  @Override
  public I get() {
    return entry.get();
  }

  @Override
  public Item asItem() {
    return entry.get().asItem();
  }

  /**
   * Gets the resource location for the given item
   * @return  Resource location for the given item
   */
  public ResourceLocation getRegistryName() {
    return Objects.requireNonNull(item.get().getRegistryName());
  }
}

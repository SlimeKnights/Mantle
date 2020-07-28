package slimeknights.mantle.registration.object;

import lombok.AllArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Objects;
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
  /** Supplier to the registry name for this entry, allows fetching the name before the entry resolves if registry object is used */
  private final Supplier<ResourceLocation> name;

  /**
   * Creates a new item object from a supplier instance. Registry name will be fetched from the supplier entry, so the entry must be present to fetch
   * @param supplier  Supplier instance
   */
  public ItemObject(Supplier<? extends I> supplier) {
    this.entry = supplier;
    this.name = () -> Objects.requireNonNull(supplier.get().getRegistryName());
  }

  /**
   * Creates a new item object using the given registry object. This variant can resolve its name before the registry object entry resolves
   * @param object  Object base
   */
  public ItemObject(RegistryObject<? extends I> object) {
    this.entry = object;
    this.name = object::getId;
  }

  /**
   * Creates a new item object using another item object. Intended to be used in a subclass to avoid an extra wrapper
   * @param object  Object base
   */
  protected ItemObject(ItemObject<? extends I> object) {
    this.entry = object.entry;
    this.name = object.name;
  }

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
    return name.get();
  }
}

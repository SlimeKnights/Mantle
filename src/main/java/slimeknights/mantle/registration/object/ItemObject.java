package slimeknights.mantle.registration.object;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registry object wrapper to implement {@link ItemConvertible}
 * @param <I>  Item class
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ItemObject<I extends ItemConvertible> implements Supplier<I>, ItemConvertible {
  /** Supplier to the registry name for this entry, allows fetching the name before the entry resolves if registry object is used */
  private final Identifier name;
  private final I entry;

  /**
   * Creates a new item object using the given registry object. This variant can resolve its name before the registry object entry resolves
   * @param entry Object base
   */
  public ItemObject(I entry) {
    this.entry = entry;
    this.name = Registry.ITEM.getId((Item) entry);
  }


  /**
   * Creates a new item object using another item object. Intended to be used in a subclass to avoid an extra wrapper
   * @param object  Object base
   */
  protected ItemObject(ItemObject<? extends I> object) {
    this.entry = object.entry;
    this.name = object.name;
  }

  /**
   * Gets the entry, throwing an exception if not present
   * @return  Entry
   * @throws NullPointerException  if not present
   */
  @Override
  public I get() {
    return Objects.requireNonNull(entry, () -> "Item Object not present " + name);
  }

  /**
   * Gets the entry, or null if its not present
   * @return  entry, or null if missing
   */
  @Nullable
  public I getOrNull() {
    return entry;
  }

  @Override
  public Item asItem() {
    return get().asItem();
  }

  /**
   * Gets the resource location for the given item
   * @return  Resource location for the given item
   */
  public Identifier getRegistryName() {
    return name;
  }
}

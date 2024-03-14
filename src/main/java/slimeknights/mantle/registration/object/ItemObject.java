package slimeknights.mantle.registration.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registry object wrapper to implement {@link ItemLike}
 * @param <I>  Item class
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@AllArgsConstructor
public class ItemObject<I extends ItemLike> implements Supplier<I>, ItemLike, IdAwareObject {
  /** Supplier to the registry entry */
  private final Supplier<? extends I> entry;
  /** Registry name for this entry, allows fetching the name before the entry resolves if registry object is used */
  @Getter
  private final ResourceLocation id;

  /**
   * Creates a new item object from a supplier instance. Registry name will be fetched from the supplier entry, so the entry must be present during construction
   * @param entry  Existing registry entry, typically a vanilla block or a registered block
   */
  public ItemObject(DefaultedRegistry<I> registry, I entry) {
    this.entry = RegistryHelper.getHolder(registry, entry);
    this.id = registry.getKey(entry);
  }

  /**
   * Creates a new item object using the given registry object. This variant can resolve its name before the registry object entry resolves
   * @param object  Object base
   */
  public ItemObject(RegistryObject<? extends I> object) {
    this.entry = object;
    this.id = object.getId();
  }

  /**
   * Creates a new item object using another item object. Intended to be used in a subclass to avoid an extra wrapper
   * @param object  Object base
   */
  protected ItemObject(ItemObject<? extends I> object) {
    this.entry = object.entry;
    this.id = object.id;
  }

  /**
   * Gets the entry, throwing an exception if not present
   * @return  Entry
   * @throws NullPointerException  if not present
   */
  @Override
  public I get() {
    return Objects.requireNonNull(entry.get(), () -> "Item Object not present " + id);
  }

  /**
   * Gets the entry, or null if its not present
   * @return  entry, or null if missing
   */
  @Nullable
  public I getOrNull() {
    try {
      return entry.get();
    } catch (NullPointerException e) {
      // thrown by RegistryObject if missing value
      return null;
    }
  }

  @Override
  public Item asItem() {
    return get().asItem();
  }
}

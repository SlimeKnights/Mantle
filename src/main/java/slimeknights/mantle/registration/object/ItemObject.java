package slimeknights.mantle.registration.object;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Registry object wrapper that also implements {@link ItemLike}. Intended for use with objects that have multiple forms.
 * @param <I>  Item class
 * @see net.neoforged.neoforge.registries.DeferredBlock
 * @see net.neoforged.neoforge.registries.DeferredItem
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ItemObject<R extends ItemLike, I extends R> extends HolderWrapper<R,I> implements ItemLike {
  /** Creates a new item object from a holder and key. */
  public ItemObject(ResourceKey<R> key, Holder<R> holder) {
    super(key, holder);
  }

  /** Creates a new item object from a holder reference. */
  public ItemObject(Holder<R> holder) {
    super(holder);
  }

  /**
   * Creates a new item object using the given registry object. This variant can resolve its name before the registry object entry resolves
   * @param holder  Object base
   */
  public ItemObject(DeferredHolder<R, I> holder) {
    super(holder);
  }

  /**
   * Creates a new item object using another item object. Intended to be used in a subclass to avoid an extra wrapper
   * @param object  Object base
   */
  protected ItemObject(ItemObject<R, ? extends I> object) {
    super(object);
  }

  @Override
  public Item asItem() {
    return holder.value().asItem();
  }
}

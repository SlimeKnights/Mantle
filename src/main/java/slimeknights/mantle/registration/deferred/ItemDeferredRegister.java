package slimeknights.mantle.registration.deferred;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import slimeknights.mantle.registration.object.ItemObject;

import java.util.function.Supplier;

/**
 * Deferred register that registers items with wrappers
 */
@SuppressWarnings("unused")
public class ItemDeferredRegister extends DeferredRegisterWrapper {

  public ItemDeferredRegister(String modID) {
    super(modID);
  }

  /**
   * Adds a new item to the list to be registered, using the given supplier
   * @param name   Item name
   * @param sup    Supplier returning an item
   * @return  Item registry object
   */
  public <I extends Item> ItemObject<I> register(String name, Supplier<I> sup) {
    return new ItemObject<>(Registry.register(Registry.ITEM, name, sup.get()));
  }

  /**
   * Adds a new item to the list to be registered, based on the given item properties
   * @param name   Item name
   * @param props  Item properties
   * @return  Item registry object
   */
  public ItemObject<Item> register(String name, Item.Settings props) {
    return register(name, () -> new Item(props));
  }
}

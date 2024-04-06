package slimeknights.mantle.registration.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register that registers items with wrappers
 */
@SuppressWarnings("unused")
public class ItemDeferredRegister extends DeferredRegisterWrapper<Item> {

  public ItemDeferredRegister(String modID) {
    super(Registries.ITEM, modID);
  }

  /**
   * Adds a new item to the list to be registered, using the given supplier
   * @param name   Item name
   * @param sup    Supplier returning an item
   * @return  Item registry object
   */
  public <I extends Item> ItemObject<I> register(String name, Supplier<? extends I> sup) {
    return new ItemObject<>(register.register(name, sup));
  }

  /**
   * Adds a new item to the list to be registered, based on the given item properties
   * @param name   Item name
   * @param props  Item properties
   * @return  Item registry object
   */
  public ItemObject<Item> register(String name, Item.Properties props) {
    return register(name, () -> new Item(props));
  }


  /* Specialty */

  /**
   * Registers an item with multiple variants, prefixing the name with the value name
   * @param values   Enum values to use for this item
   * @param name     Name of the block
   * @param mapper   Function to get a item for the given enum value
   * @return  EnumObject mapping between different item types
   */
  public <T extends Enum<T> & StringRepresentable, I extends Item> EnumObject<T,I> registerEnum(T[] values, String name, Function<T,? extends I> mapper) {
    return registerEnum(values, name, (fullName, type) -> register(fullName, () -> mapper.apply(type)));
  }

  /**
   * Registers an item with multiple variants, suffixing the name with the value name
   * @param values   Enum values to use for this item
   * @param name     Name of the block
   * @param mapper   Function to get a item for the given enum value
   * @return  EnumObject mapping between different item types
   */
  public <T extends Enum<T> & StringRepresentable, I extends Item> EnumObject<T,I> registerEnum(String name, T[] values, Function<T,? extends I> mapper) {
    return registerEnum(name, values, (fullName, type) -> register(fullName, () -> mapper.apply(type)));
  }
}

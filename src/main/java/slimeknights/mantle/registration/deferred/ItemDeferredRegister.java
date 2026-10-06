package slimeknights.mantle.registration.deferred;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Deferred register that registers items.
 */
@SuppressWarnings("unused")
public class ItemDeferredRegister extends EnumDeferredRegister<Item> {
  public ItemDeferredRegister(String modID) {
    super(Registries.ITEM, modID);
  }

  @Override
  protected <I extends Item> DeferredItem<I> createHolder(ResourceKey<? extends Registry<Item>> registryKey, ResourceLocation key) {
    return DeferredItem.createItem(ResourceKey.create(registryKey, key));
  }

  /**
   * Adds a new item to the list to be registered, using the given supplier
   * @param name   Item name
   * @param item   Function mapping the item name to instance
   * @return  Item registry object
   */
  @SuppressWarnings("unchecked")
  @Override
  public <I extends Item> DeferredItem<I> register(String name, Function<ResourceLocation, ? extends I> item) {
    return (DeferredItem<I>) super.register(name, item);
  }

  /**
   * Adds a new item to the list to be registered, using the given supplier
   * @param name   Item name
   * @param item    Supplier returning an item
   * @return  Item registry object
   */
  @SuppressWarnings("unchecked")
  @Override
  public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> item) {
    return (DeferredItem<I>) super.register(name, item);
  }

  /**
   * Adds a new item to the list to be registered, based on the given item properties
   * @param name   Item name
   * @param props  Item properties
   * @return  Item registry object
   */
  public DeferredItem<Item> register(String name, Item.Properties props) {
    return register(name, () -> new Item(props));
  }

  /**
   * Adds a new item to the list to be registered, with default item properties
   * @param name   Item name
   * @return  Item registry object
   */
  public DeferredItem<Item> register(String name) {
    return register(name, new Item.Properties());
  }
}

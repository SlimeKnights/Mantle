package slimeknights.mantle.client.model;

import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;

import java.util.function.Function;

/** Logic to map from an item stack to a string key for {@link ItemKeyModel} */
public interface ItemKey extends IHaveLoader {
  /** Loader registry instance */
  GenericLoaderRegistry<ItemKey> LOADER = new GenericLoaderRegistry<>("Item Model Key", true);

  /**
   * Gets the texture key for the given stack instance.
   * @param stack  Item stack instance
   * @return  Key, or an empty string if the data is not present
   */
  String getKey(ItemStack stack);

  @Override
  RecordLoadable<? extends ItemKey> getLoader();


  /** Creates a simple item key getter */
  static ItemKey simple(Function<ItemStack, String> getter) {
    return SingletonLoader.singleton(loader -> new ItemKey() {
      @Override
      public String getKey(ItemStack stack) {
        return getter.apply(stack);
      }

      @Override
      public RecordLoadable<? extends ItemKey> getLoader() {
        return loader;
      }
    });
  }
}

package slimeknights.mantle.data.predicate.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.loader.RegistrySetLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;

import java.util.Set;

/** Predicate matching an item from a set */
public record ItemSetPredicate(Set<Item> items) implements ItemPredicate {
  public static final IGenericLoader<ItemSetPredicate> LOADER = new RegistrySetLoader<>("items", ForgeRegistries.ITEMS, ItemSetPredicate::new, predicate -> predicate.items);

  public ItemSetPredicate(Item item) {
    this(Set.of(item));
  }

  @Override
  public boolean matches(Item item) {
    return items.contains(item);
  }

  @Override
  public IGenericLoader<? extends IJsonPredicate<Item>> getLoader() {
    return LOADER;
  }
}

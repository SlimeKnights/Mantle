package slimeknights.mantle.data.predicate.item;

import net.minecraft.world.item.Item;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;

import java.util.Set;

/** Predicate matching an item from a set */
public record ItemSetPredicate(Set<Item> items) implements ItemPredicate {
  public static final RecordLoadable<ItemSetPredicate> LOADER = RecordLoadable.create(Loadables.ITEM.set().field("items", ItemSetPredicate::items), ItemSetPredicate::new);

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

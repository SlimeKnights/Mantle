package slimeknights.mantle.data.predicate.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.util.RegistryHelper;

/** Predicate matching an item tag */
public record ItemTagPredicate(TagKey<Item> tag) implements ItemPredicate {
  public static final RecordLoadable<ItemTagPredicate> LOADER = RecordLoadable.create(Loadables.ITEM_TAG.field("tag", ItemTagPredicate::tag), ItemTagPredicate::new);

  @Override
  public boolean matches(Item item) {
    return RegistryHelper.contains(tag, item);
  }

  @Override
  public IGenericLoader<? extends ItemPredicate> getLoader() {
    return LOADER;
  }
}

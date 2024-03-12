package slimeknights.mantle.data.predicate.item;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.loader.TagKeyLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.util.RegistryHelper;

/** Predicate matching an item tag */
public record ItemTagPredicate(TagKey<Item> tag) implements ItemPredicate {
  public static final TagKeyLoader<Item,ItemTagPredicate> LOADER = new TagKeyLoader<>(Registry.ITEM_REGISTRY, ItemTagPredicate::new, c -> c.tag);

  @Override
  public boolean matches(Item item) {
    return RegistryHelper.contains(tag, item);
  }

  @Override
  public IGenericLoader<? extends IJsonPredicate<Item>> getLoader() {
    return LOADER;
  }
}

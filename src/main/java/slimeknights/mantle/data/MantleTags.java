package slimeknights.mantle.data;

import net.minecraft.world.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
import slimeknights.mantle.Mantle;

/**
 * All tags used in Mantle logic
 */
public class MantleTags {
  /** Initializes the tags */
  public static void init() {
    Items.init();
  }

  public static class Items {
    private static void init() {}

    /** Items in this tag will render offhand attack indicators */
    public static IOptionalNamedTag<Item> OFFHAND_COOLDOWN = ItemTags.createOptional(Mantle.getResource("offhand_cooldown"));
  }
}

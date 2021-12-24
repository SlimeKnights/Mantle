package slimeknights.mantle.registration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemProperties {
  /** Properties for a standard bucket item */
  public static final Item.Properties BUCKET_PROPS = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(CreativeModeTab.TAB_MISC);

  /** Item property used for spawn egg items */
  public static final Item.Properties EGG_PROPS = new Item.Properties().tab(CreativeModeTab.TAB_MISC);
}

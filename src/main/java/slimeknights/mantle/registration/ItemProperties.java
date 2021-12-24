package slimeknights.mantle.registration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemProperties {
  /** Properties for a standard bucket item */
  public static final Item.Properties BUCKET_PROPS = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(ItemGroup.TAB_MISC);

  /** Item property used for spawn egg items */
  public static final Item.Properties EGG_PROPS = new Item.Properties().tab(ItemGroup.TAB_MISC);
}

package slimeknights.mantle.registration;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;

public class ItemProperties {
  /** Properties for a standard bucket item */
  public static final Item.Settings BUCKET_PROPS = new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1).group(ItemGroup.MISC);

  /** Item property used for spawn egg items */
  public static final Item.Settings EGG_PROPS = new Item.Settings().group(ItemGroup.MISC);
}

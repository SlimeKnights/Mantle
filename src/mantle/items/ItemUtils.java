package mantle.items;

import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameData;

public class ItemUtils
{
    public static String getUniqueName (Item item)
    {
        return GameData.getItemRegistry().getNameForObject(item);
    }
    public static Item getItemFromUniqueName (String uniqueName)
    {
        return GameData.getItemRegistry().getObject(uniqueName);
    }
}

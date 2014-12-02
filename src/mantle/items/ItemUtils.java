package mantle.items;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameData;

public class ItemUtils
{
    public static String getUniqueName (Item item)
    {
        return (String)GameData.getItemRegistry().getNameForObject(item);
    }
    public static Item getItemFromUniqueName (String uniqueName)
    {
        return GameData.getItemRegistry().getObject(uniqueName);
    }
}

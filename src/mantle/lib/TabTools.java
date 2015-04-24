package mantle.lib;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TabTools extends CreativeTabs
{
    ItemStack display;

    public TabTools(String label)
    {
        super(label);
    }

    public void init(ItemStack stack)
    {
        this.display = stack;
    }

    @Override
    public ItemStack getIconItemStack()
    {
        return this.display;
    }

    @Override
    public Item getTabIconItem()
    {
        return this.display.getItem();
    }
}

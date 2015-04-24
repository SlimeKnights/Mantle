package mantle.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMetaWrapper
{
    public final Item item;

    public final Integer meta;

    public ItemMetaWrapper(Item item, Integer meta)
    {
        this.item = item;
        this.meta = meta;
    }

    public ItemMetaWrapper(ItemStack stack)
    {
        this.item = stack.getItem();
        this.meta = stack.getItemDamage();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || this.getClass() != o.getClass())
        {
            return false;
        }

        ItemMetaWrapper itemMeta = (ItemMetaWrapper) o;

        if (this.item != null ? !(this.item == itemMeta.item) : itemMeta.item != null)
        {
            return false;
        }
        return !(this.meta != null ? !this.meta.equals(itemMeta.meta) : itemMeta.meta != null);

    }

    @Override
    public int hashCode()
    {
        int result = this.item != null ? this.item.hashCode() : 0;
        result = 31 * result + (this.meta != null ? this.meta.hashCode() : 0);
        return result;
    }
}

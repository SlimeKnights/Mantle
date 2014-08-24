package mantle.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemMetaWrapper {
    public final Item item;
    public final Integer meta;

    public ItemMetaWrapper(Item item, Integer meta) {
        this.item = item;
        this.meta = meta;
    }

    public ItemMetaWrapper(ItemStack stack)
    {
        this.item = stack.getItem();
        this.meta = stack.getItemDamage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemMetaWrapper itemMeta = (ItemMetaWrapper) o;

        if (item != null ? !(item == itemMeta.item) : itemMeta.item != null) return false;
        if (meta != null ? !meta.equals(itemMeta.meta) : itemMeta.meta != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = item != null ? item.hashCode() : 0;
        result = 31 * result + (meta != null ? meta.hashCode() : 0);
        return result;
    }
}

package mantle.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackWrapper extends ItemMetaWrapper {
    public final Integer stacksize;

    public ItemStackWrapper(Item item, Integer meta, Integer stacksize) {
        super(item, meta);
        this.stacksize = stacksize;
    }

    public ItemStackWrapper(ItemStack stack, Integer stacksize) {
        super(stack);
        this.stacksize = stacksize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ItemStackWrapper that = (ItemStackWrapper) o;

        return !(stacksize != null ? !stacksize.equals(that.stacksize) : that.stacksize != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (stacksize != null ? stacksize.hashCode() : 0);
        return result;
    }
}

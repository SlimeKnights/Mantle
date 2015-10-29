package mantle.utils;

import net.minecraft.item.ItemStack;

public class ItemStackNBTWrapper {
    public final ItemStack stack;

    public ItemStackNBTWrapper(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemStackNBTWrapper that = (ItemStackNBTWrapper) o;

        return ItemStack.areItemStacksEqual(this.stack, that.stack);
    }

    @Override
    public int hashCode() {
        int result = stack.getItem() != null ? stack.getItem().hashCode() : 0;
        result = 31 * result + stack.getMetadata();
        result = 31 * result + (stack.getTagCompound() != null ? stack.getTagCompound().hashCode() : 0);
        result = 31 * result + stack.stackSize;
        return result;
    }
}

package mantle.utils;

import net.minecraft.item.ItemStack;

public class ItemStackNBTWrapper
{
    public final ItemStack stack;

    public ItemStackNBTWrapper(ItemStack stack)
    {
        this.stack = stack;
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

        ItemStackNBTWrapper that = (ItemStackNBTWrapper) o;

        return ItemStack.areItemStacksEqual(this.stack, that.stack);
    }

    @Override
    public int hashCode()
    {
        int result = this.stack.getItem() != null ? this.stack.getItem().hashCode() : 0;
        result = 31 * result + this.stack.getItemDamage();
        result = 31 * result + (this.stack.getTagCompound() != null ? this.stack.getTagCompound().hashCode() : 0);
        result = 31 * result + this.stack.stackSize;
        return result;
    }
}

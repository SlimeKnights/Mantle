package mantle.blocks.abstracts;

import java.util.Random;

import mantle.blocks.iface.IFacingLogic;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AdaptiveInventoryLogic extends InventoryLogic implements IFacingLogic
{
    Random random = new Random();

    protected int inventorySize;

    public AdaptiveInventoryLogic()
    {
        super(0);
    }

    protected void adjustInventory(int size, boolean forceAdjust)
    {
        if (size != this.inventorySize || forceAdjust)
        {
            this.inventorySize = size;

            ItemStack[] tempInv = this.inventory;
            this.inventory = new ItemStack[size];
            int invLength = tempInv.length > this.inventory.length ? this.inventory.length : tempInv.length;
            System.arraycopy(tempInv, 0, this.inventory, 0, invLength);

            if (tempInv.length > this.inventory.length)
            {
                for (int i = this.inventory.length; i < tempInv.length; i++)
                {
                    ItemStack stack = tempInv[i];
                    if (stack != null)
                    {
                        float jumpX = this.random.nextFloat() * 0.8F + 0.1F;
                        float jumpY = this.random.nextFloat() * 0.8F + 0.1F;
                        float jumpZ = this.random.nextFloat() * 0.8F + 0.1F;

                        int offsetX = 0;
                        int offsetY = 0;
                        int offsetZ = 0;
                        switch (this.getTossDirection())
                        {
                        case 0: // -y
                            offsetY--;
                            break;
                        case 1: // +y
                            offsetY++;
                            break;
                        case 2: // +z
                            offsetZ--;
                            break;
                        case 3: // -z
                            offsetZ++;
                            break;
                        case 4: // +x
                            offsetX--;
                            break;
                        case 5: // -x
                            offsetX++;
                            break;
                        }

                        while (stack.stackSize > 0)
                        {
                            int itemSize = this.random.nextInt(21) + 10;

                            if (itemSize > stack.stackSize)
                            {
                                itemSize = stack.stackSize;
                            }

                            stack.stackSize -= itemSize;
                            EntityItem entityitem = new EntityItem(this.worldObj, (double) ((float) this.pos.getX() + jumpX + offsetX), (double) ((float) this.pos.getY() + jumpY),
                                    (double) ((float) this.pos.getZ() + jumpZ + offsetZ), new ItemStack(stack.getItem(), itemSize, stack.getItemDamage()));

                            if (stack.hasTagCompound())
                            {
                                entityitem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
                            }

                            float offset = 0.05F;
                            entityitem.motionX = (double) ((float) this.random.nextGaussian() * offset);
                            entityitem.motionY = (double) ((float) this.random.nextGaussian() * offset + 0.2F);
                            entityitem.motionZ = (double) ((float) this.random.nextGaussian() * offset);
                            this.worldObj.spawnEntityInWorld(entityitem);
                        }
                    }
                }
            }
        }
    }

    public int getTossDirection()
    {
        return this.getRenderDirection();
    }
}

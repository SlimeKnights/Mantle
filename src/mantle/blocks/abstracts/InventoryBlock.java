package mantle.blocks.abstracts;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import mantle.debug.DebugHelper;
import mantle.debug.IDebuggable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import mantle.blocks.iface.IActiveLogic;
import mantle.blocks.iface.IFacingLogic;

import java.util.Random;

/**
 * Abstract for blocks with inventories.
 *
 * @author mDiyo
 */
public abstract class InventoryBlock extends BlockContainer
{
    protected Random rand = new Random();

    protected InventoryBlock(Material material)
    {
        super(material);
    }

    //TODO createNewTileEntity()
    /* Logic backend */
    public TileEntity func_149915_a (World var1)
    {
        return null;
    }

    public abstract TileEntity createTileEntity (World world, int metadata);

    public abstract Integer getGui (World world, int x, int y, int z, EntityPlayer entityplayer);

    public abstract Object getModInstance ();

    //TODO:         onBlockActivated()
    @Override
    public boolean func_149727_a (World world, int x, int y, int z, EntityPlayer player, int side, float clickX, float clickY, float clickZ)
    {
        if (player.isSneaking())
            return false;

        Integer integer = getGui(world, x, y, z, player);
        if (integer == null || integer == -1)
        {
            return false;
        }
        else
        {
            if (!world.isRemote)
                player.openGui(getModInstance(), integer, world, x, y, z);
            return true;
        }
    }

    /* Inventory */
    //TODO:     breakBlock()
    @Override
    public void func_149749_a (World par1World, int x, int y, int z, Block block, int meta)
    {
        TileEntity te = par1World.func_147438_o(x, y, z);

        if (te != null && te instanceof InventoryLogic)
        {
            InventoryLogic logic = (InventoryLogic) te;
            logic.removeBlock();
            for (int iter = 0; iter < logic.getSizeInventory(); ++iter)
            {
                ItemStack stack = logic.getStackInSlot(iter);

                if (stack != null && logic.canDropInventorySlot(iter))
                {
                    float jumpX = rand.nextFloat() * 0.8F + 0.1F;
                    float jumpY = rand.nextFloat() * 0.8F + 0.1F;
                    float jumpZ = rand.nextFloat() * 0.8F + 0.1F;

                    while (stack.stackSize > 0)
                    {
                        int itemSize = rand.nextInt(21) + 10;

                        if (itemSize > stack.stackSize)
                        {
                            itemSize = stack.stackSize;
                        }

                        stack.stackSize -= itemSize;
                        EntityItem entityitem = new EntityItem(par1World, (double) ((float) x + jumpX), (double) ((float) y + jumpY), (double) ((float) z + jumpZ), new ItemStack(stack.getItem(),
                                itemSize, stack.getItemDamage()));

                        if (stack.hasTagCompound())
                        {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
                        }

                        float offset = 0.05F;
                        entityitem.motionX = (double) ((float) rand.nextGaussian() * offset);
                        entityitem.motionY = (double) ((float) rand.nextGaussian() * offset + 0.2F);
                        entityitem.motionZ = (double) ((float) rand.nextGaussian() * offset);
                        par1World.spawnEntityInWorld(entityitem);
                    }
                }
            }
        }
        super.func_149749_a(par1World, x, y, z, block, meta);
    }

    /* Placement */

    int side = -1;

    //This class does not have an actual block placed in the world
   //TODO onBlockPlaced
    @Override
    public int func_149660_a (World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
    {
        this.side = side;
        return meta;
    }
    //TODO onBlockPlacedBy
    @Override
    public void func_149689_a (World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack stack)
    {
        TileEntity logic = world.func_147438_o(x, y, z);
        if (logic instanceof IFacingLogic)
        {
            IFacingLogic direction = (IFacingLogic) logic;
            if (side != -1)
            {
                direction.setDirection(side);
                side = -1;
            }
            if (entityliving == null)
            {
                direction.setDirection(0F, 0F, null);
            }
            else
            {
                direction.setDirection(entityliving.rotationYaw * 4F, entityliving.rotationPitch, entityliving);
            }
        }

        if (logic instanceof InventoryLogic)
        {
            InventoryLogic inv = (InventoryLogic) logic;
            inv.placeBlock(entityliving, stack);
            if (stack.hasDisplayName())
            {
                inv.setInvName(stack.getDisplayName());
            }
        }
    }

    public static boolean isActive (IBlockAccess world, int x, int y, int z)
    {
        TileEntity logic = world.func_147438_o(x, y, z);
        if (logic instanceof IActiveLogic)
        {
            return ((IActiveLogic) logic).getActive();
        }
        return false;
    }

    public int damageDropped (int meta)
    {
        return meta;
    }

    /* Textures */
    public IIcon[] icons;

    public abstract String[] getTextureNames ();

    @Override
    public void func_149651_a (IIconRegister iconRegister)
    {
        String[] textureNames = getTextureNames();
        this.icons = new IIcon[textureNames.length];

        for (int i = 0; i < this.icons.length; ++i)
        {
            this.icons[i] = iconRegister.registerIcon("tinker:" + textureNames[i]);
        }
    }

    /* IDebuggable */
    @Override
    public void func_149699_a(World world, int x, int y, int z, EntityPlayer player) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && player.getHeldItem().getItem() == Item.func_150899_d(000)) //TODO make this get a stick!!
        {
            TileEntity te = world.func_147438_o(x, y, z);
            if (te instanceof IDebuggable)
            {
                DebugHelper.handleDebugData(((IDebuggable) te).getDebugInfo(player));
            }
        }

        super.func_149699_a(world, x, y, z, player);
    }
}

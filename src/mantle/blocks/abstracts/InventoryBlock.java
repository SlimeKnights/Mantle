package mantle.blocks.abstracts;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import mantle.blocks.iface.IActiveLogic;
import mantle.blocks.iface.IFacingLogic;
import mantle.debug.DebugHelper;
import mantle.debug.IDebuggable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class InventoryBlock extends BlockContainer
{
    protected Random rand = new Random();

    protected InventoryBlock(Material material)
    {
        super(material);
    }

    /* Logic backend */
    public TileEntity createNewTileEntity (World var1)
    {
        return null;
    }

    public abstract TileEntity createNewTileEntity (World world, int metadata);

    public abstract Integer getGui (World world, int x, int y, int z, EntityPlayer entityplayer);

    public abstract Object getModInstance ();

    @Override
    public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float clickX, float clickY, float clickZ)
    {
        if (player.isSneaking())
            return false;

        Integer integer = getGui(world, pos.getX(), pos.getY(), pos.getZ(), player);
        if (integer == null || integer == -1)
        {
            return false;
        }
        else
        {
            if (!world.isRemote)
                player.openGui(getModInstance(), integer, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
    }

    /* Inventory */
    @Override
    public void breakBlock (World par1World, BlockPos pos, IBlockState meta)
    {
        TileEntity te = par1World.getTileEntity(pos);

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
                        EntityItem entityitem = new EntityItem(par1World, (double) ((float) pos.getX() + jumpX), (double) ((float) pos.getY() + jumpY), (double) ((float) pos.getZ() + jumpZ), new ItemStack(stack.getItem(),
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

        super.breakBlock(par1World, pos, meta);
    }

    /* Placement */

    EnumFacing side;

    //This class does not have an actual block placed in the world
    @Override
    public IBlockState onBlockPlaced (World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        this.side = side;
        return world.getBlockState(pos).getBlock().getStateFromMeta(meta);
    }

    @Override
    @SuppressWarnings("deprecation") // TODO: Remove this when setDirection calls updated.
    public void onBlockPlacedBy (World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack)
    {
        TileEntity logic = world.getTileEntity(pos);
        if (logic instanceof IFacingLogic)
        {
            IFacingLogic direction = (IFacingLogic) logic;
            // TODO: Convert all setDirection calls to modern invokation, when that's ready.
            if (side != null)
            {
                direction.setDirection(side, 0F, 0F, null);
                side = null;
            }
            if (entityliving == null)
            {
                direction.setDirection(null, 0F, 0F, null);
            }
            else
            {
                direction.setDirection(null, entityliving.rotationYaw * 4F, entityliving.rotationPitch, entityliving);
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

    @SuppressWarnings("unused")
    public static boolean isActive (IBlockAccess world, BlockPos pos)
    {
        TileEntity logic = world.getTileEntity(pos);
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

    public abstract String getTextureDomain (int textureNameIndex);

    @Override
    public void registerBlockIcons (IIconRegister iconRegister)
    {
        String[] textureNames = getTextureNames();
        this.icons = new IIcon[textureNames.length];

        for (int i = 0; i < this.icons.length; ++i)
        {
            this.icons[i] = iconRegister.registerIcon(getTextureDomain(i) + ":" + textureNames[i]);
        }
    }

    /* IDebuggable */
    @Override
    public void onBlockClicked (World world, BlockPos pos, EntityPlayer player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && player.getHeldItem() != null &&  player.getHeldItem().getItem() != null && player.getHeldItem().getItem() == Items.stick)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IDebuggable)
            {
                DebugHelper.handleDebugData(((IDebuggable) te).getDebugInfo(player));
            }
        }

        super.onBlockClicked(world, pos, player);
    }
}
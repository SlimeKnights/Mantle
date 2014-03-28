package mantle.blocks;

import mantle.debug.DebugHelper;
import mantle.debug.IDebuggable;
import mantle.items.iface.IDebugItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

/**
 * Root class for inheriting the Minecraft Block.
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public abstract class MantleBlock extends Block
{

    public MantleBlock(Material material)
    {
        super(material);
    }

    // IDebuggable support - Uses a stick for debug purposes.
    @Override
    public void onBlockClicked (World world, int x, int y, int z, EntityPlayer player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && player.getHeldItem() != null
                && (player.getHeldItem().getItem() == Items.stick || player.getHeldItem().getItem() instanceof IDebugItem))
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof IDebuggable)
                DebugHelper.handleDebugData(((IDebuggable) te).getDebugInfo(player));
        }
        super.onBlockClicked(world, x, y, z, player);
    }

}

package mantle.blocks;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import mantle.debug.DebugHelper;
import mantle.debug.IDebuggable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Root class for inheriting the Minecraft Block.
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public abstract class MantleBlock extends Block {

    public MantleBlock(Material material) {
        super(material);
    }

    // IDebuggable support - Uses a stick for debug purposes.
    //TODO onBlockClicked
    @Override
    public void func_149699_a(World world, int x, int y, int z, EntityPlayer player) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER && player.getHeldItem() != null && player.getHeldItem().getItem() == Item.func_150898_a(Block.func_149684_b("Item_Stick")))
        {
            TileEntity te = world.func_147438_o(x, y, z);
            if (te instanceof IDebuggable)
                DebugHelper.handleDebugData(((IDebuggable) te).getDebugInfo(player));
        }
        super.func_149699_a(world, x, y, z, player);
    }

}

package mantle.blocks.iface;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Interface for blocks which can be whacked with debug tools (sticks?) for information.
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public interface IDebuggable {

    public void sendDebugToPlayer(EntityPlayer player);

}

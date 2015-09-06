package mantle.debug;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Interface for TEs which can be whacked with debug tools (sticks normally) for information.
 *
 * This is automatically used if the block containing the TE is a subclass of MantleBlock.
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public interface IDebuggable
{

    public DebugData getDebugInfo(EntityPlayer player);

}

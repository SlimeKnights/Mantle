package mantle.debug;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Encapsulation for debug information
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public class DebugData
{

    public final EntityPlayer player;
    @SuppressWarnings("rawtypes") public final Class cl;
    public final String[] strings;

    @SuppressWarnings("rawtypes")
    public DebugData(EntityPlayer player, Class cl, String[] strings)
    {
        this.player = player;
        this.cl = cl;
        this.strings = strings;
    }

}

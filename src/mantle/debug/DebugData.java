package mantle.debug;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Encapsulation for debug information
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public class DebugData {

    public final EntityPlayer player;
    public final Class cl;
    public final String[] strings;

    public DebugData(EntityPlayer player, Class cl, String[] strings)
    {
        this.player = player;
        this.cl = cl;
        this.strings = strings;
    }

}

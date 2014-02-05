package mantle.debug;

import org.apache.commons.lang3.StringUtils;

import mantle.player.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import static mantle.lib.CoreRepo.*;
import static mantle.lib.CoreConfig.*;

/**
 * IDebuggable helper
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public class DebugHelper
{

    private DebugHelper()
    {
    } //Singleton

    public static void handleDebugData (DebugData data)
    {
        if (debug_enableChat)
            handleChatDebug(data);
        if (debug_enableConsole)
            handleConsoleDebug(data);
    }

    private static void handleChatDebug (DebugData data)
    {
        EntityPlayer player = data.player;
        String prefix = "[" + data.cl.getSimpleName() + "] ";
        for (String str : data.strings)
        {
            PlayerUtils.sendChatMessage(player, prefix + str);
        }
    }

    private static void handleConsoleDebug (DebugData data)
    {
        String player = data.player.getDisplayName();//.username;
        String prefix = "[" + player + ":" + data.cl.getSimpleName() + "] ";
        for (String str : data.strings)
        {
            logger.info(prefix + str);
        }
    }

}

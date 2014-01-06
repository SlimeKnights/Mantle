package mantle.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

/**
 * Misc utils for dealing with players.
 * 
 * @author progwml6
 */
public class PlayerUtils
{
    
    /**
     * Sends a given message to a specific player (due to 1.7 axing addChatMessage())
     */
    public static void sendChatMessage (EntityPlayer p, String message)
    {
        if (p != null && message != null && message.isEmpty())
            p.func_146105_b(new ChatComponentText(message));
    }
    
}

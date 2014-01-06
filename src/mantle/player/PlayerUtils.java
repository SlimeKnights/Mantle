package mantle.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class PlayerUtils
{
    public static void sendChatMessage (EntityPlayer p, String message)
    {
        if (p != null && message != null && message.isEmpty())
            p.func_146105_b(new ChatComponentText(message));
    }
}

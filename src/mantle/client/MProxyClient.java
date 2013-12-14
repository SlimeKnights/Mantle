package mantle.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import mantle.client.block.SmallFontRenderer;
import mantle.client.pages.BlankPage;
import mantle.client.pages.BookPage;
import mantle.client.pages.CraftingPage;


public class MProxyClient
{
    public static SmallFontRenderer smallFontRenderer;

    /* Registers any rendering code. */
    public void registerRenderer ()
    {
        Minecraft mc = Minecraft.getMinecraft();
        smallFontRenderer = new SmallFontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);
    }

    public static Map<String, Class<? extends BookPage>> pageClasses = new HashMap<String, Class<? extends BookPage>>();

    public static void registerManualPage (String type, Class<? extends BookPage> clazz)
    {
        pageClasses.put(type, clazz);
    }

    public static Class<? extends BookPage> getPageClass (String type)
    {
        return pageClasses.get(type);
    }

}

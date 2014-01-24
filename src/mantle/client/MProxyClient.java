package mantle.client;

import mantle.books.BookDataStore;
import mantle.common.MProxyCommon;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import mantle.client.block.SmallFontRenderer;
import mantle.client.gui.GuiManual;
import mantle.client.pages.*;
import mantle.common.MProxyCommon;

public class MProxyClient extends MProxyCommon
{
    public static SmallFontRenderer smallFontRenderer;

    /* Registers any rendering code. */
    public void registerRenderer ()
    {
        Minecraft mc = Minecraft.getMinecraft();
        smallFontRenderer = new SmallFontRenderer(mc.gameSettings, new ResourceLocation("minecraft:textures/font/ascii.png"), mc.renderEngine, false);
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
    void initManualPages ()
    {
        MProxyClient.registerManualPage("crafting", CraftingPage.class);
        MProxyClient.registerManualPage("picture", PicturePage.class);
        MProxyClient.registerManualPage("text", TextPage.class);
        MProxyClient.registerManualPage("intro", TextPage.class);
        MProxyClient.registerManualPage("sectionpage", SectionPage.class);
        MProxyClient.registerManualPage("intro", TitlePage.class);
        MProxyClient.registerManualPage("contents", ContentsTablePage.class);
        MProxyClient.registerManualPage("furnace", FurnacePage.class);
        MProxyClient.registerManualPage("sidebar", SidebarPage.class);
        MProxyClient.registerManualPage("blank", BlankPage.class);
    }
    @Override
    public Object getClientGuiElement (int ID, EntityPlayer player, World world, int x, int y, int z)
    {
    if (ID == manualGuiID)
    {
        ItemStack stack = player.getCurrentEquippedItem();
        return new GuiManual(stack, MProxyClient.getManualFromStack(stack));
    }
    return null;
    }

    private static Document getManualFromStack (ItemStack stack)
    {
        return BookDataStore.getBookFromName(stack.getItem().getUnlocalizedName()).getDoc();
    }
}

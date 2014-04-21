package mantle.client;

import static mantle.lib.CoreRepo.logger;

import java.util.HashMap;
import java.util.Map;

import mantle.books.BookData;
import mantle.books.BookDataStore;
import mantle.client.gui.GuiManual;
import mantle.client.pages.BlankPage;
import mantle.client.pages.BookPage;
import mantle.client.pages.ContentsTablePage;
import mantle.client.pages.CraftingPage;
import mantle.client.pages.FurnacePage;
import mantle.client.pages.PicturePage;
import mantle.client.pages.SectionPage;
import mantle.client.pages.SidebarPage;
import mantle.client.pages.TextPage;
import mantle.client.pages.TitlePage;
import mantle.common.MProxyCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class MProxyClient extends MProxyCommon
{

    public static final String MINECRAFT_ASCII_PATH = "minecraft:textures/font/ascii.png";

    public static SmallFontRenderer smallFontRenderer;

    /* Registers any rendering code. */
    public void registerRenderer ()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getTextureManager() == null)
            logger.error("Vanilla texture manager is null!");
        if (mc.renderEngine == null)
            logger.error("Vanilla render engine is null!");
        smallFontRenderer = new SmallFontRenderer(mc.gameSettings, new ResourceLocation(MINECRAFT_ASCII_PATH), mc.renderEngine, false);
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

    public void readManuals ()
    {
        initManualPages();
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
            if (stack == null)
                logger.error("Null stack in book.");
            if (stack != null && stack.getItem() == null)
                logger.error("Null item in book.");
            if (stack != null && stack.getItem() != null && stack.getItem().getUnlocalizedName() == null)
                logger.error("Null unlocalized name in book.");
            else
            {
                return new GuiManual(stack, MProxyClient.getBookDataFromStack(stack));
            }
        }
        return null;
    }

    private static BookData getBookDataFromStack (ItemStack stack)
    {
        return BookDataStore.getBookFromName(stack.getItem().getUnlocalizedName(stack));
    }
}

package mantle.client;

import mantle.books.BookData;
import mantle.books.BookDataStore;
import mantle.common.MProxyCommon;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import mantle.client.block.SmallFontRenderer;
import mantle.client.gui.GuiManual;
import mantle.client.pages.*;
import static mantle.lib.CoreRepo.logger;

public class MProxyClient extends MProxyCommon
{
    public static SmallFontRenderer smallFontRenderer;

    /* Registers any rendering code. */
    public void registerRenderer ()
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getTextureManager() == null)
            logger.error("vanilla texture man is null");
        if (mc.renderEngine == null)
            logger.error("vanilla render engine is null");
        smallFontRenderer = new SmallFontRenderer(mc.gameSettings, new ResourceLocation("minecraft:textures/font/ascii.png"), mc.renderEngine, false);
    }

    public static Map<String, Class<? extends BookPage>> pageClasses = new HashMap<String, Class<? extends BookPage>>();

    public static void registerManualPage (String type, Class<? extends BookPage> clazz)
    {
        pageClasses.put(type, clazz);
    }

    public static Class<? extends BookPage> getPageClass (String type)
    {
        // logger.info(type);
        //logger.info(pageClasses.get(type).getName());
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
                logger.error("stack null in book");
            if (stack != null && stack.getItem() == null)
                logger.error("item null in book");
            if (stack != null && stack.getItem() != null && stack.getItem().getUnlocalizedName() == null)
                logger.error("unlocalized name null in book");
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

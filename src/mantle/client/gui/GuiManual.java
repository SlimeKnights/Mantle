package mantle.client.gui;

import mantle.books.BookData;
import mantle.client.MProxyClient;
import mantle.client.pages.BookPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SideOnly(Side.CLIENT)
public class GuiManual extends GuiScreen
{
    ItemStack itemstackBook;

    Document manual;

    public RenderItem renderitem = Minecraft.getMinecraft().getRenderItem();

    int bookImageWidth = 206;

    int bookImageHeight = 200;

    int bookTotalPages = 1;

    int currentPage;

    int maxPages;

    BookData bData;

    private TurnPageButton buttonNextPage;

    private TurnPageButton buttonPreviousPage;

    private static ResourceLocation bookRight;// = new ResourceLocation("mantle", "textures/gui/bookright.png");

    private static ResourceLocation bookLeft;// = new ResourceLocation("mantle", "textures/gui/bookleft.png");

    BookPage pageLeft;

    BookPage pageRight;

    public FontRenderer fonts = Minecraft.getMinecraft().fontRendererObj;

    public GuiManual(ItemStack stack, BookData data)
    {
        this.mc = Minecraft.getMinecraft();
        this.itemstackBook = stack;
        this.currentPage = 0; //Stack page
        this.manual = data.getDoc();
        if (data.font != null)
        {
            this.fonts = data.font;
        }
        bookLeft = data.leftImage;
        bookRight = data.rightImage;
        this.bData = data;

        //renderitem.renderInFrame = true;
    }

    /*@Override
    public void setWorldAndResolution (Minecraft minecraft, int w, int h)
    {
        this.guiParticles = new GuiParticle(minecraft);
        this.mc = minecraft;
        this.width = w;
        this.height = h;
        this.buttonList.clear();
        this.initGui();
    }*/

    @Override
    @SuppressWarnings("unchecked")
    public void initGui()
    {
        this.maxPages = this.manual.getElementsByTagName("page").getLength();
        this.updateText();
        int xPos = (this.width) / 2; //TODO Width?
        //TODO buttonList
        this.buttonList.add(this.buttonNextPage = new TurnPageButton(1, xPos + this.bookImageWidth - 50, 180, true, this.bData));
        this.buttonList.add(this.buttonPreviousPage = new TurnPageButton(2, xPos - this.bookImageWidth + 24, 180, false, this.bData));
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 1)
            {
                this.currentPage += 2;
            }
            if (button.id == 2)
            {
                this.currentPage -= 2;
            }

            this.updateText();
        }
    }

    void updateText()
    {
        if (this.maxPages % 2 == 1)
        {
            if (this.currentPage > this.maxPages)
            {
                this.currentPage = this.maxPages;
            }
        }
        else
        {
            if (this.currentPage >= this.maxPages)
            {
                this.currentPage = this.maxPages - 2;
            }
        }
        if (this.currentPage % 2 == 1)
        {
            this.currentPage--;
        }
        if (this.currentPage < 0)
        {
            this.currentPage = 0;
        }

        NodeList nList = this.manual.getElementsByTagName("page");

        Node node = nList.item(this.currentPage);
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            Element element = (Element) node;
            Class<? extends BookPage> clazz = MProxyClient.getPageClass(element.getAttribute("type"));
            if (clazz != null)
            {
                try
                {
                    this.pageLeft = clazz.newInstance();
                    this.pageLeft.init(this, 0);
                    this.pageLeft.readPageFromXML(element);
                }
                catch (Exception e)
                {
                }
            }
            else
            {
                this.pageLeft = null;
            }
        }

        node = nList.item(this.currentPage + 1);
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
        {
            Element element = (Element) node;
            Class<? extends BookPage> clazz = MProxyClient.getPageClass(element.getAttribute("type"));
            if (clazz != null)
            {
                try
                {
                    this.pageRight = clazz.newInstance();
                    this.pageRight.init(this, 1);
                    this.pageRight.readPageFromXML(element);
                }
                catch (Exception e)
                {
                }
            }
            else
            {
                this.pageLeft = null;
            }
        }
        else
        {
            this.pageRight = null;
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookRight);
        int localWidth = (this.width / 2);
        byte localHeight = 8;
        this.drawTexturedModalRect(localWidth, localHeight, 0, 0, this.bookImageWidth, this.bookImageHeight);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookLeft);
        localWidth = localWidth - this.bookImageWidth;
        this.drawTexturedModalRect(localWidth, localHeight, 256 - this.bookImageWidth, 0, this.bookImageWidth, this.bookImageHeight);

        super.drawScreen(par1, par2, par3); //16, 12, 220, 12

        if (this.pageLeft != null)
        {
            this.pageLeft.renderBackgroundLayer(localWidth + 16, localHeight + 12);
        }
        if (this.pageRight != null)
        {
            this.pageRight.renderBackgroundLayer(localWidth + 220, localHeight + 12);
        }
        if (this.pageLeft != null)
        {
            this.pageLeft.renderContentLayer(localWidth + 16, localHeight + 12, this.bData.isTranslatable);
        }
        if (this.pageRight != null)
        {
            this.pageRight.renderContentLayer(localWidth + 220, localHeight + 12, this.bData.isTranslatable);
        }

    }

    public Minecraft getMC()
    {
        return this.mc;
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}

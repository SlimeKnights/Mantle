package mantle.client.pages;

import mantle.lib.client.MantleClientRegistry;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ContentsTablePage extends BookPage
{
    String text;

    String[] iconText;

    ItemStack[] icons;

    @Override
    public void readPageFromXML(Element element)
    {
        NodeList nodes = element.getElementsByTagName("text");
        if (nodes != null)
        {
            this.text = nodes.item(0).getTextContent();
        }

        nodes = element.getElementsByTagName("link");
        this.iconText = new String[nodes.getLength()];
        this.icons = new ItemStack[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++)
        {
            NodeList children = nodes.item(i).getChildNodes();
            this.iconText[i] = children.item(1).getTextContent();
            this.icons[i] = MantleClientRegistry.getManualIcon(children.item(3).getTextContent());
        }
    }

    @Override
    public void renderContentLayer(int localWidth, int localHeight, boolean isTranslatable)
    {
        if (this.text != null)
        {
            if (isTranslatable)
            {
                this.text = StatCollector.translateToLocal(this.text);
            }
            this.manual.fonts.drawString("\u00a7n" + this.text, localWidth + 25 + this.manual.fonts.getStringWidth(this.text) / 2, localHeight + 4, 0);
        }
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        this.manual.renderitem.zLevel = 100;
        for (int i = 0; i < this.icons.length; i++)
        {
            this.manual.renderitem.renderItemIntoGUI(this.icons[i], localWidth + 16, localHeight + 18 * i + 18);
            int yOffset = 18;
            if (isTranslatable)
            {
                this.iconText[i] = StatCollector.translateToLocal(this.iconText[i]);
            }

            if (this.iconText[i].length() > 40)
            {
                yOffset = 13;
            }
            this.manual.fonts.drawString(this.iconText[i], localWidth + 38, localHeight + 18 * i + yOffset, 0);
        }
        this.manual.renderitem.zLevel = 0;
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }
}

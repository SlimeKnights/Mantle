package mantle.client.pages;

import net.minecraft.util.StatCollector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SectionPage extends BookPage
{
    String title;

    String body;

    @Override
    public void readPageFromXML(Element element)
    {
        NodeList nodes = element.getElementsByTagName("title");
        if (nodes != null)
        {
            this.title = nodes.item(0).getTextContent();
        }

        nodes = element.getElementsByTagName("text");
        if (nodes != null)
        {
            this.body = nodes.item(0).getTextContent();
        }
    }

    @Override
    public void renderContentLayer(int localWidth, int localHeight, boolean isTranslatable)
    {
        if (isTranslatable)
        {
            this.title = StatCollector.translateToLocal(this.title);
            this.body = StatCollector.translateToLocal(this.body);
        }
        this.manual.fonts.drawSplitString("\u00a7n" + this.title, localWidth + 70, localHeight + 4, 178, 0);
        this.manual.fonts.drawSplitString(this.body, localWidth, localHeight + 16, 190, 0);
    }
}

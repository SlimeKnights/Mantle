package mantle.client.pages;

import net.minecraft.util.StatCollector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TextPage extends BookPage
{
    String text;

    @Override
    public void readPageFromXML(Element element)
    {
        NodeList nodes = element.getElementsByTagName("text");
        if (nodes != null)
        {
            this.text = nodes.item(0).getTextContent();
        }
    }

    @Override
    public void renderContentLayer(int localWidth, int localHeight, boolean IsTranslatable)
    {
        if (IsTranslatable)
        {
            this.text = StatCollector.translateToLocal(this.text);
        }
        this.manual.fonts.drawSplitString(this.text, localWidth, localHeight, 178, 0);
    }
}

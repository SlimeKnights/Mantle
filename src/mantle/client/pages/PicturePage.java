package mantle.client.pages;

import mantle.lib.client.MantleClientRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import static mantle.lib.CoreRepo.logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PicturePage extends BookPage
{
    String text;
    String location;
    ResourceLocation background;

    @Override
    public void readPageFromXML (Element element)
    {
        NodeList nodes = element.getElementsByTagName("text");
        if (nodes != null)
            text = nodes.item(0).getTextContent();
        nodes = element.getElementsByTagName("location");
        if (nodes != null)
        {
            location = nodes.item(0).getTextContent();
            background = new ResourceLocation(location);
            if(background == null)
            {
                logger.warn(nodes.item(0).getTextContent() + " could not be found in the image cache(location)!");
            }
        }

        //this borked stuffs somehow unless someone is interested in using it this stays disabled for now
       /* nodes = element.getElementsByTagName("bookImage");
        if (nodes != null)
        {
            //TODO 1.7 null check all of this
            background = MantleClientRegistry.imageCache.get(nodes.item(0).getTextContent()).resource;
            if(background == null){
                logger.warn(nodes.item(0).getTextContent() + " could not be found in the image cache(bookimage)!");
            }
        }*/

    }

    @Override
    public void renderContentLayer (int localWidth, int localHeight, boolean isTranslatable)
    {
        if (isTranslatable)
            text = StatCollector.translateToLocal(text);
        manual.fonts.drawSplitString(text, localWidth + 8, localHeight, 178, 0);
    }

    public void renderBackgroundLayer (int localWidth, int localHeight)
    {
        if (background != null)
        {
            manual.getMC().getTextureManager().bindTexture(background);
        }
        //manual.getMC().renderEngine.bindTexture(location);
        manual.drawTexturedModalRect(localWidth, localHeight + 12, 0, 0, 170, 144);
    }
}

package mantle.client.pages;

import static mantle.lib.CoreRepo.logger;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PicturePage extends BookPage
{
    String text;

    String location;

    ResourceLocation background;

    @Override
    public void readPageFromXML(Element element)
    {
        NodeList nodes = element.getElementsByTagName("text");
        if (nodes != null)
        {
            this.text = nodes.item(0).getTextContent();
        }
        nodes = element.getElementsByTagName("location");
        if (nodes != null)
        {
            this.location = nodes.item(0).getTextContent();
            this.background = new ResourceLocation(this.location);
            if (this.background == null)
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
    public void renderContentLayer(int localWidth, int localHeight, boolean isTranslatable)
    {
        if (isTranslatable)
        {
            this.text = StatCollector.translateToLocal(this.text);
        }
        this.manual.fonts.drawSplitString(this.text, localWidth + 8, localHeight, 178, 0);
    }

    @Override
    public void renderBackgroundLayer(int localWidth, int localHeight)
    {
        if (this.background != null)
        {
            this.manual.getMC().getTextureManager().bindTexture(this.background);
        }
        //manual.getMC().renderEngine.bindTexture(location);
        this.manual.drawTexturedModalRect(localWidth, localHeight + 12, 0, 0, 170, 144);
    }
}

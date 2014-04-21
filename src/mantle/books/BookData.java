package mantle.books;

import mantle.client.SmallFontRenderer;
import net.minecraft.util.ResourceLocation;

import org.w3c.dom.Document;

public class BookData
{
    public String unlocalizedName = new String();
    public String toolTip = new String();
    public String modID = new String();
    public ResourceLocation leftImage = new ResourceLocation("mantle", "textures/gui/bookleft.png");
    public ResourceLocation rightImage = new ResourceLocation("mantle", "textures/gui/bookright.png");
    public ResourceLocation itemImage = new ResourceLocation("mantle", "textures/items/mantlebook_blue.png");
    public Document doc = ManualReader.readManual("/assets/mantle/manuals/test.xml");
    //font can be left null if so, the default from mantle will be used
    public SmallFontRenderer font;
    public Boolean isTranslatable = false;
    public boolean isFromZip = false;

    public Document getDoc ()
    {
        return this.doc;
    }

    public String getFullUnlocalizedName ()
    {
        return this.modID + ":" + this.unlocalizedName;
    }

}

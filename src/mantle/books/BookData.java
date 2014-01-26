package mantle.books;

import mantle.client.block.SmallFontRenderer;
import net.minecraft.util.ResourceLocation;

import org.w3c.dom.Document;

public class BookData
{
    public final String unlocalizedName = new String();
    public final String toolTip = new String();
    public final String modID = new String();
    public final ResourceLocation leftImage = new ResourceLocation("mantle", "textures/gui/bookleft.png");
    public final ResourceLocation rightImage = new ResourceLocation("mantle", "textures/gui/bookright.png");
    private final Document doc = ManualReader.readManual("/assets/mantle/manuals/test.xml");
    //font can be left null if so, the default from mantle will be used
    public SmallFontRenderer font;
    public final Boolean isTranslatable = false;

    public Document getDoc ()
    {
        return this.doc;
    }

    public String getFullUnlocalizedName ()
    {
        return this.modID + ":" + this.unlocalizedName;
    }

}

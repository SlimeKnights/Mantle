package mantle.books;

import mantle.client.block.SmallFontRenderer;

import org.w3c.dom.Document;

public class BookData
{
    public final String unlocalizedName = new String();
    public final String toolTip = new String();
    public final String modID = new String();
    public final String leftImageLocation = new String("textures/gui/bookleft.png");
    public final String rightImageLocation = new String("textures/gui/bookright.png");
    private final Document doc = ManualReader.readManual("/assets/mantle/manuals/test.xml");
    //font can be left null if so, the default from mantle will be used
    public SmallFontRenderer font;
    public Document getDoc ()
    {
        return this.doc;
    }

    public String getFullUnlocalizedName ()
    {
        return this.modID + ":" + this.unlocalizedName;
    }

}

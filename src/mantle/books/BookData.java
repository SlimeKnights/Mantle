package mantle.books;

import org.w3c.dom.Document;

public class BookData
{
    public final String unlocalizedName = new String();
    public final String toolTip = new String();
    public final String modID = new String();
    public final String LeftImageLocation = new String();
    public final String rightImageLocation = new String();
    public final Document doc = ManualReader.readManual("/assets/mantle/manuals/test.xml");

    public Document getDoc ()
    {
        return this.doc;
    }

    public String getFullUnlocalizedName ()
    {
        return this.modID + ":" + this.unlocalizedName;
    }

}

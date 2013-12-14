package mantle.client.pages;

import org.w3c.dom.Element;

import mantle.client.gui.GuiManual;

public abstract class BookPage
{
    protected GuiManual manual;
    protected int side;

    public void init (GuiManual manual, int side)
    {
        this.manual = manual;
        this.side = side;
    }

    public abstract void readPageFromXML (Element element);

    public void renderBackgroundLayer (int localwidth, int localheight)
    {
    }

    public abstract void renderContentLayer (int localwidth, int localheight);
}

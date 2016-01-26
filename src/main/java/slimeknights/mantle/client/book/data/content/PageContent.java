package slimeknights.mantle.client.book.data.content;

import java.util.ArrayList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.gui.book.element.BookElement;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public abstract class PageContent {

  public abstract void build(ArrayList<BookElement> list);
}

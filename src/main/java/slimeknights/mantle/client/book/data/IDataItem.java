package slimeknights.mantle.client.book.data;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author fuj1n
 */
@SideOnly(Side.CLIENT)
public interface IDataItem {

  int cascadeLoad();
}

package slimeknights.mantle.client.book.data;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AppearanceData implements IDataItem {

  public int coverColor = 0x8B4631;
  public int arrowColor = 0xFFFFD3;
  public int arrowColorHover = 0xFF541C;
  public String title = "";
  public String subtitle = "";
  public boolean drawPageNumbers = true;

  @Override
  public int cascadeLoad() {
    return 0;
  }
}

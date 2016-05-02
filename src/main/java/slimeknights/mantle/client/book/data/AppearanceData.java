package slimeknights.mantle.client.book.data;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AppearanceData implements IDataItem {

  public int coverColor = 0x8B4631;
  public int arrowColor = 0xFFFFD3;
  public int arrowColorHover = 0xFF541C;
  public int hoverColor = 0x77EE541C;
  public int slotColor = 0xFF844C;
  public int lockedSectionColor = 0x000000;
  public float scale = 0.5F;
  public String title = "";
  public String subtitle = "";
  public boolean drawPageNumbers = true;
  public boolean drawSectionListText = false;

  @Override
  public void load() {

  }
}

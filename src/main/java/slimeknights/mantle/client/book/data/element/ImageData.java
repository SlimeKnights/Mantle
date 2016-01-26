package slimeknights.mantle.client.book.data.element;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ImageData {

  public static final ImageData MISSING = new ImageData();

  public String file = "";
  public int u = 0;
  public int v = 0;
  public int uw = 256;
  public int vh = 256;
  public int texWidth = 256;
  public int texHeight = 256;
  public int x = -1;
  public int y = -1;
  public int width = -1;
  public int height = -1;

  public transient ResourceLocation location;

  static {
    MISSING.location = new ResourceLocation("mantle:textures/gui/missingno.png");
    MISSING.texWidth = 32;
    MISSING.texHeight = 32;
    MISSING.uw = 32;
    MISSING.vh = 32;
  }
}

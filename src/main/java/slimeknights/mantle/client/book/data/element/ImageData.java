package slimeknights.mantle.client.book.data.element;

import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.client.book.repository.BookRepository;

public class ImageData extends DataLocation {

  public static final ImageData MISSING = new ImageData();

  public IngredientData item = null;
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
  public int colorMultiplier = 0xFFFFFF;

  public ImageData() {
  }

  public ImageData(ResourceLocation location, int u, int v, int uw, int vh, int texWidth, int texHeight) {
    this(location, u, v, uw, vh, texWidth, texHeight, uw, vh, 0xFFFFFF);
  }

  public ImageData(ResourceLocation location, int u, int v, int uw, int vh, int texWidth, int texHeight, int colorMultiplier) {
    this(location, u, v, uw, vh, texWidth, texHeight, uw, vh);
  }

  public ImageData(ResourceLocation location, int u, int v, int uw, int vh, int texWidth, int texHeight, int width, int height) {
    this(location, u, v, uw, vh, texWidth, texHeight, width, height, 0xFFFFFF);
  }

  public ImageData(ResourceLocation location, int u, int v, int uw, int vh, int texWidth, int texHeight, int width, int height, int colorMultiplier) {
    this.location = location;
    this.u = u;
    this.v = v;
    this.uw = uw;
    this.vh = vh;
    this.texWidth = texWidth;
    this.texHeight = texHeight;
    this.width = width;
    this.height = height;
    this.colorMultiplier = colorMultiplier;
  }

  static {
    MISSING.location = new ResourceLocation("mantle:textures/gui/missingno.png");
    MISSING.texWidth = 32;
    MISSING.texHeight = 32;
    MISSING.uw = 32;
    MISSING.vh = 32;
  }

  @Override
  public void load(BookRepository source) {
    super.load(source);

    if (this.item != null) {
      this.item.load(source);
    }
  }
}

package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.mantle.client.book.data.element.ImageData;

@SideOnly(Side.CLIENT)
public class ElementImage extends SizedBookElement {

  public ImageData image;

  public ElementImage(ImageData image) {
    this(image.x, image.y, image.width, image.height, image);
  }

  public ElementImage(int x, int y, int width, int height, ImageData image) {
    super(x, y, width, height);

    this.image = image;

    if (image.x != -1)
      x = image.x;
    if (image.y != -1)
      y = image.y;
    if (image.width != -1)
      width = image.width;
    if (image.height != -1)
      height = image.height;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks) {
    Minecraft.getMinecraft().renderEngine.bindTexture(image.location);

    drawScaledCustomSizeModalRect(x, y, image.u, image.v, image.uw, image.vh, width, height, image.texWidth, image.texHeight);
  }
}

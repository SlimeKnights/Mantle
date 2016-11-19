package slimeknights.mantle.client.gui.book.element;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import slimeknights.mantle.client.book.data.element.ImageData;

@SideOnly(Side.CLIENT)
public class ElementImage extends SizedBookElement {

  public ImageData image;
  public int colorMultiplier;

  public ElementImage(ImageData image) {
    this(image, 0xFFFFFF);
  }

  public ElementImage(ImageData image, int colorMultiplier) {
    this(image.x, image.y, image.width, image.height, image, colorMultiplier);
  }

  public ElementImage(int x, int y, int width, int height, ImageData image) {
    this(x, y, width, height, image, image.colorMultiplier);
  }

  public ElementImage(int x, int y, int width, int height, ImageData image, int colorMultiplier) {
    super(x, y, width, height);

    this.image = image;

    if(image.x != -1) {
      x = image.x;
    }
    if(image.y != -1) {
      y = image.y;
    }
    if(image.width != -1) {
      width = image.width;
    }
    if(image.height != -1) {
      height = image.height;
    }
    if(image.colorMultiplier != 0xFFFFFF) {
      colorMultiplier = image.colorMultiplier;
    }

    this.x = x == -1 ? 0 : x;
    this.y = y == -1 ? 0 : y;
    this.width = width;
    this.height = height;
    this.colorMultiplier = colorMultiplier;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    float r = ((colorMultiplier >> 16) & 0xff) / 255.F;
    float g = ((colorMultiplier >> 8) & 0xff) / 255.F;
    float b = (colorMultiplier & 0xff) / 255.F;

    GlStateManager.color(r, g, b);

    if(image.item == null) {
      renderEngine.bindTexture(image.location);

      drawScaledCustomSizeModalRect(x, y, image.u, image.v, image.uw, image.vh, width, height, image.texWidth, image.texHeight);
    } else {
      GlStateManager.pushMatrix();
      GlStateManager.scale(width, height, 1F);
      RenderHelper.enableGUIStandardItemLighting();
      mc.getRenderItem().renderItemAndEffectIntoGUI(image.item.getItems().get(0), x, y);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.popMatrix();
    }
  }
}

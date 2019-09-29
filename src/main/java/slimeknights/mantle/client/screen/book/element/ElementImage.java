package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.data.element.ImageData;

@OnlyIn(Dist.CLIENT)
public class ElementImage extends SizedBookElement {

  public ImageData image;
  public int colorMultiplier;

  private ElementItem itemElement;

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

    if (image.x != -1) {
      x = image.x;
    }
    if (image.y != -1) {
      y = image.y;
    }
    if (image.width != -1) {
      width = image.width;
    }
    if (image.height != -1) {
      height = image.height;
    }
    if (image.colorMultiplier != 0xFFFFFF) {
      colorMultiplier = image.colorMultiplier;
    }

    this.x = x == -1 ? 0 : x;
    this.y = y == -1 ? 0 : y;
    this.width = width;
    this.height = height;
    this.colorMultiplier = colorMultiplier;

    if(image.item != null) {
      itemElement = new ElementItem(0, 0, 1F, image.item.getItems());
    }
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    float r = ((this.colorMultiplier >> 16) & 0xff) / 255.F;
    float g = ((this.colorMultiplier >> 8) & 0xff) / 255.F;
    float b = (this.colorMultiplier & 0xff) / 255.F;

    GlStateManager.color3f(r, g, b);

    if (this.image.item == null) {
      this.renderEngine.bindTexture(this.image.location);

      blitRaw(x, y, width, height, image.u, image.u + image.uw, image.v, image.v + image.vh, image.texWidth, image.texHeight);
    }
    else {
      GlStateManager.pushMatrix();
      GlStateManager.translatef(this.x, this.y, 0F);
      GlStateManager.scalef(this.width / 16F, this.height / 16F, 1F);
      itemElement.draw(mouseX, mouseY, partialTicks, fontRenderer);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.popMatrix();
    }
  }

  public static void blitRaw(int x, int y, int w, int h, int minU, int maxU, int minV, int maxV, float tw, float th) {
    innerBlit(x, x + w, y, y + h, 0, minU / tw, maxU / tw, minV / th, maxV / th);
  }
}

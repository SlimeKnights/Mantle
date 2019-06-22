package slimeknights.mantle.client.gui.book.element;

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
    float r = ((this.colorMultiplier >> 16) & 0xff) / 255.F;
    float g = ((this.colorMultiplier >> 8) & 0xff) / 255.F;
    float b = (this.colorMultiplier & 0xff) / 255.F;

    GlStateManager.color3f(r, g, b);

    if(this.image.item == null) {
      this.renderEngine.bindTexture(this.image.location);

      blit(this.x, this.y, this.image.u, this.image.v, this.image.uw, this.image.vh, this.width, this.height, this.image.texWidth, this.image.texHeight);
    } else {
      GlStateManager.pushMatrix();
      GlStateManager.scalef(this.width, this.height, 1F);
      RenderHelper.enableGUIStandardItemLighting();
      this.mc.getItemRenderer().renderItemAndEffectIntoGUI(this.image.item.getItems().get(0), this.x, this.y);
      RenderHelper.disableStandardItemLighting();
      GlStateManager.popMatrix();
    }
  }
}

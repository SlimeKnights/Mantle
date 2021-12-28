package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureManager;
import slimeknights.mantle.client.book.data.element.ImageData;

import static java.util.Objects.requireNonNullElse;

public class ImageElement extends SizedBookElement {

  public ImageData image;
  public int colorMultiplier;

  private ItemElement itemElement;

  public ImageElement(ImageData image) {
    this(image, 0xFFFFFF);
  }

  public ImageElement(ImageData image, int colorMultiplier) {
    this(image.x, image.y, image.width, image.height, image, colorMultiplier);
  }

  public ImageElement(int x, int y, int width, int height, ImageData image) {
    this(x, y, width, height, image, image.colorMultiplier);
  }

  public ImageElement(int x, int y, int width, int height, ImageData image, int colorMultiplier) {
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
      this.itemElement = new ItemElement(0, 0, 1F, image.item.getItems());
    }
  }

  @Override
  public void draw(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    float r = ((this.colorMultiplier >> 16) & 0xff) / 255.F;
    float g = ((this.colorMultiplier >> 8) & 0xff) / 255.F;
    float b = (this.colorMultiplier & 0xff) / 255.F;

    RenderSystem.setShaderColor(r, g, b, 1f);

    if (this.image.item == null) {
      RenderSystem.setShaderTexture(0, requireNonNullElse(this.image.location, TextureManager.INTENTIONAL_MISSING_TEXTURE));
      blitRaw(matrixStack, this.x, this.y, this.width, this.height, this.image.u, this.image.u + this.image.uw, this.image.v, this.image.v + this.image.vh, this.image.texWidth, this.image.texHeight);
    }
    else {
      matrixStack.pushPose();
      matrixStack.translate(this.x, this.y, 0F);
      matrixStack.scale(this.width / 16F, this.height / 16F, 1F);

      this.itemElement.draw(matrixStack, mouseX, mouseY, partialTicks, fontRenderer);

      matrixStack.popPose();
    }
  }

  public static void blitRaw(PoseStack matrixStack, int x, int y, int w, int h, int minU, int maxU, int minV, int maxV, float tw, float th) {
    innerBlit(matrixStack.last().pose(), x, x + w, y, y + h, 0, minU / tw, maxU / tw, minV / th, maxV / th);
  }
}

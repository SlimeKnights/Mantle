package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.client.book.data.BookData;

import javax.annotation.Nullable;

import static slimeknights.mantle.client.screen.book.Textures.TEX_BOOK;

public class ArrowButton extends Button {

  public static final int WIDTH = 18;
  public static final int HEIGHT = 10;

  // Appearance
  @Nullable
  private final BookData bookData;
  public ArrowType arrowType;
  public int color;
  public int hoverColor;

  public ArrowButton(@Nullable BookData bookData, int x, int y, ArrowType arrowType, int color, int hoverColor, OnPress iPressable) {
    super(x, y, arrowType.w, arrowType.h, Component.empty(), iPressable);

    this.arrowType = arrowType;
    this.color = color;
    this.hoverColor = hoverColor;
    this.bookData = bookData;
  }

  public ArrowButton(int x, int y, ArrowType arrowType, int color, int hoverColor, OnPress iPressable) {
    this(null, x, y, arrowType, color, hoverColor, iPressable);
  }

  public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, @Nullable BookData bookData) {
    Minecraft minecraft = Minecraft.getInstance();
    if (bookData != null) {
      RenderSystem.setShaderTexture(0, bookData.appearance.getBookTexture());
    } else {
      RenderSystem.setShaderTexture(0, TEX_BOOK);
    }

    this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

    int color = this.isHovered ? this.hoverColor : this.color;

    float r = ((color >> 16) & 0xff) / 255.F;
    float g = ((color >> 8) & 0xff) / 255.F;
    float b = (color & 0xff) / 255.F;

    RenderSystem.setShaderColor(r, g, b, 1f);
    blit(matrixStack, this.x, this.y, this.width, this.height, this.arrowType.x, this.arrowType.y, this.width, this.height, 512, 512);
    this.renderBg(matrixStack, minecraft, mouseX, mouseY);
  }

  @Override
  public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    renderButton(matrixStack, mouseX, mouseY, partialTicks, bookData);
  }

  public enum ArrowType {
    NEXT(412, 0),
    PREV(412, 10),
    RIGHT(412, 20),
    LEFT(412, 30),
    BACK_UP(412, 40, 18, 18),
    UP(412, 58, 10, 18),
    DOWN(412 + 10, 58, 10, 18),
    REFRESH(412, 76, 18, 18);

    public final int x, y, w, h;

    ArrowType(int x, int y) {
      this(x, y, WIDTH, HEIGHT);
    }

    ArrowType(int x, int y, int w, int h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }
  }
}

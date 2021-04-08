package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import static slimeknights.mantle.client.screen.book.Textures.TEX_BOOK;

@Environment(EnvType.CLIENT)
public class ArrowButton extends ButtonWidget {

  public static final int WIDTH = 18;
  public static final int HEIGHT = 10;

  // Appearance
  public ArrowType arrowType;
  public int color;
  public int hoverColor;

  public ArrowButton(int x, int y, ArrowType arrowType, int color, int hoverColor, PressAction iPressable) {
    super(x, y, arrowType.w, arrowType.h, LiteralText.EMPTY, iPressable);

    this.arrowType = arrowType;
    this.color = color;
    this.hoverColor = hoverColor;
  }

  @Override
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    MinecraftClient minecraft = MinecraftClient.getInstance();
    minecraft.getTextureManager().bindTexture(TEX_BOOK);

    this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

    int color = this.hovered ? this.hoverColor : this.color;

    float r = ((color >> 16) & 0xff) / 255.F;
    float g = ((color >> 8) & 0xff) / 255.F;
    float b = (color & 0xff) / 255.F;

    RenderSystem.color3f(r, g, b);
    drawTexture(matrixStack, this.x, this.y, this.width, this.height, this.arrowType.x, this.arrowType.y, this.width, this.height, 512, 512);
    this.renderBg(matrixStack, minecraft, mouseX, mouseY);
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

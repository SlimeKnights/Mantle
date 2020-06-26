package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static slimeknights.mantle.client.screen.book.Textures.TEX_BOOK;

@OnlyIn(Dist.CLIENT)
public class ArrowButton extends Button {

  public static final int WIDTH = 18;
  public static final int HEIGHT = 10;

  // Appearance
  public ArrowType arrowType;
  public int color;
  public int hoverColor;

  public ArrowButton(int x, int y, ArrowType arrowType, int color, int hoverColor, IPressable iPressable) {
    super(x, y, arrowType.w, arrowType.h, StringTextComponent.field_240750_d_, iPressable);

    this.arrowType = arrowType;
    this.color = color;
    this.hoverColor = hoverColor;
  }

  @Override
  public void func_230431_b_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.getTextureManager().bindTexture(TEX_BOOK);

    this.field_230692_n_ = mouseX >= this.field_230690_l_ && mouseY >= this.field_230691_m_ && mouseX < this.field_230690_l_ + this.field_230688_j_ && mouseY < this.field_230691_m_ + this.field_230689_k_;

    int color = this.field_230692_n_ ? this.hoverColor : this.color;

    float r = ((color >> 16) & 0xff) / 255.F;
    float g = ((color >> 8) & 0xff) / 255.F;
    float b = (color & 0xff) / 255.F;

    RenderSystem.color3f(r, g, b);
    func_238466_a_(matrixStack, this.field_230690_l_, this.field_230691_m_, this.field_230688_j_, this.field_230689_k_, this.arrowType.x, this.arrowType.y, this.field_230688_j_, this.field_230689_k_, 512, 512);
    this.func_230441_a_(matrixStack, minecraft, mouseX, mouseY);
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

package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.data.BookmarkData;

@OnlyIn(Dist.CLIENT)
@Deprecated // Unused and to be removed in upcoming book rework.
public class BookmarkButton extends Button {

  private static final ResourceLocation TEX_BOOK = new ResourceLocation("mantle:textures/gui/book.png");

  public static final int WIDTH = 31;
  public static final int HEIGHT = 9;
  public static final int TEX_X = 0, TEX_Y = 400;
  public static final int ADD_W = 5, ADD_H = 5, ADD_X = 32, ADD_Y = 402;

  public static final float TEXT_SCALE = 0.5F;

  public int type = 0;

  public final BookmarkData data;

  public BookmarkButton(BookmarkData data, IPressable iPressable) {
    super(-500, -500, WIDTH, HEIGHT, StringTextComponent.EMPTY, iPressable);

    this.data = data;
  }

  @Override
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    int tex_y = TEX_Y + HEIGHT * this.type;

    minecraft.textureManager.bindTexture(TEX_BOOK);

    float r = ((this.data.color >> 16) & 0xff) / 255.F;
    float g = ((this.data.color >> 8) & 0xff) / 255.F;
    float b = (this.data.color & 0xff) / 255.F;

    RenderSystem.color3f(r, g, b);
    blit(matrixStack, this.x, this.y, this.width, this.y, TEX_X, tex_y, WIDTH, HEIGHT, 512, 512);

    if (this.data.text != null && !this.data.text.isEmpty()) {
      TextDataRenderer.drawScaledString(matrixStack, minecraft.fontRenderer, this.data.text, this.x + 1, this.y + this.y / 2 - minecraft.fontRenderer.FONT_HEIGHT * TEXT_SCALE / 2 + 1, 0xFFFFFFFF, true, TEXT_SCALE);
    }

    RenderSystem.color3f(1F, 1F, 1F);

    if (this.data.page.equals("ADD")) {
      blit(matrixStack, this.x + this.width / 2 - ADD_W / 2, this.y + this.y / 2 - ADD_H / 2, ADD_X, ADD_Y, ADD_W, ADD_H, 512, 512);
    }
  }
}

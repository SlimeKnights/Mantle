package slimeknights.mantle.client.gui.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import slimeknights.mantle.client.book.data.BookmarkData;

public class GuiBookmark extends GuiButton {

  private static final ResourceLocation TEX_BOOK = new ResourceLocation("mantle:textures/gui/book.png");

  public static final int WIDTH = 31;
  public static final int HEIGHT = 9;
  public static final int TEX_X = 0, TEX_Y = 400;
  public static final int ADD_W = 5, ADD_H = 5, ADD_X = 32, ADD_Y = 402;

  public static final float TEXT_SCALE = 0.5F;

  public int type = 0;

  public final BookmarkData data;

  public GuiBookmark(BookmarkData data) {
    super(-1337, -500, -500, WIDTH, HEIGHT, "");

    this.data = data;
  }

  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY) {
    if(visible) {
      int tex_y = TEX_Y + HEIGHT * type;

      mc.renderEngine.bindTexture(TEX_BOOK);

      float r = ((data.color >> 16) & 0xff) / 255.F;
      float g = ((data.color >> 8) & 0xff) / 255.F;
      float b = (data.color & 0xff) / 255.F;

      GlStateManager.color(r, g, b);
      Gui.drawScaledCustomSizeModalRect(xPosition, yPosition, TEX_X, tex_y, WIDTH, HEIGHT, width, height, 512, 512);

      if(data.text != null && !data.text.isEmpty()) {
        TextDataRenderer
            .drawScaledString(mc.fontRendererObj, data.text, xPosition + 1, yPosition + height / 2 - mc.fontRendererObj.FONT_HEIGHT * TEXT_SCALE / 2 + 1, 0xFFFFFFFF, true, TEXT_SCALE);
      }

      GlStateManager.color(1F, 1F, 1F);

      if(data.page.equals("ADD")) {
        Gui.drawModalRectWithCustomSizedTexture(xPosition + width / 2 - ADD_W / 2, yPosition + height / 2 - ADD_H / 2, ADD_X, ADD_Y, ADD_W, ADD_H, 512, 512);
      }
    }
  }
}

package slimeknights.mantle.client.gui.book.editor;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.gui.book.BoxRenderer;
import slimeknights.mantle.client.gui.book.GuiBook;
import slimeknights.mantle.client.gui.book.Textures;

public class GuiBookEditor extends GuiScreen {

  private static final int SIDEBAR_WIDTH = 120;

  private GuiBook innerUi;
  private int side;

  public GuiBookEditor(@Nullable BookData book) {
    if (book == null)
      book = new BookData();

    innerUi = new GuiBook(book, null, null);
    innerUi.mc = Minecraft.getMinecraft();
    innerUi.doneLoading();

    innerUi._setPage(-1);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    GlStateManager.pushMatrix();

    TextureManager render = this.mc.renderEngine;

    if (mc.gameSettings.guiScale == 1) {
      GlStateManager.scale(2F, 2F, 2F);

      mouseX /= 2;
      mouseY /= 2;
    }

    BoxRenderer.drawBox(width / 2 - (GuiBook.PAGE_WIDTH_UNSCALED + SIDEBAR_WIDTH) / 2, height / 2 - (GuiBook.PAGE_HEIGHT_UNSCALED - GuiBook.PAGE_PADDING) / 2, SIDEBAR_WIDTH + GuiBook.PAGE_PADDING + GuiBook.PAGE_MARGIN, GuiBook.PAGE_HEIGHT_UNSCALED - GuiBook.PAGE_PADDING, 0);

    if (innerUi.getPage_() == -1) {
      GlStateManager.pushMatrix();
      if (mc.gameSettings.guiScale == 1)
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
      innerUi.openCover();
      innerUi.drawScreen(mouseX * (mc.gameSettings.guiScale == 1 ? 2 : 1), mouseY * (mc.gameSettings.guiScale == 1 ? 2 : 1), partialTicks);
      GlStateManager.popMatrix();
    } else {
      render.bindTexture(Textures.TEX_BOOK);

      GlStateManager.color(1F, 1F, 1F);
      drawModalRectWithCustomSizedTexture(width / 2 - GuiBook.PAGE_WIDTH_UNSCALED / 2, height / 2 - GuiBook.PAGE_HEIGHT_UNSCALED / 2, 0, GuiBook.PAGE_HEIGHT_UNSCALED, GuiBook.PAGE_WIDTH_UNSCALED - GuiBook.PAGE_MARGIN - GuiBook.PAGE_PADDING, GuiBook.PAGE_HEIGHT_UNSCALED, GuiBook.TEX_SIZE, GuiBook.TEX_SIZE);
      drawModalRectWithCustomSizedTexture(width / 2 - GuiBook.PAGE_WIDTH_UNSCALED / 2 + GuiBook.PAGE_MARGIN + GuiBook.PAGE_PADDING, height / 2 - GuiBook.PAGE_HEIGHT_UNSCALED / 2, GuiBook.PAGE_WIDTH_UNSCALED + GuiBook.PAGE_MARGIN + GuiBook.PAGE_PADDING, GuiBook.PAGE_HEIGHT_UNSCALED, GuiBook.PAGE_WIDTH_UNSCALED - GuiBook.PAGE_MARGIN - GuiBook.PAGE_PADDING, GuiBook.PAGE_HEIGHT_UNSCALED, GuiBook.TEX_SIZE, GuiBook.TEX_SIZE);
    }

    super.drawScreen(mouseX, mouseY, partialTicks);
    GlStateManager.popMatrix();
  }

  @Override
  public void initGui() {
    buttonList.clear();

    innerUi.width = width + SIDEBAR_WIDTH * (mc.gameSettings.guiScale == 1 ? 2 : 1);
    innerUi.height = height;

    if (mc.gameSettings.guiScale == 1) {
      width /= 2F;
      height /= 2F;
    }

    innerUi.initGui();
    innerUi.updateScreen();
    innerUi.buttonList.clear();

    buttonList.add(new GuiButton(0, 0, height - 20, 50, 20, "Import"));
    buttonList.add(new GuiButton(1, 55, height - 20, 50, 20, "Export"));
  }
}

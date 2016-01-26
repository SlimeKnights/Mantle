package slimeknights.mantle.client.gui.book;

import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.gui.book.element.BookElement;

@SideOnly(Side.CLIENT)
public class GuiBook extends GuiScreen {

  private static final ResourceLocation TEX_BOOK = new ResourceLocation("mantle:textures/gui/book.png");
  private static final ResourceLocation TEX_BOOKFRONT = new ResourceLocation("mantle:textures/gui/bookfront.png");

  public static final int PAGE_WIDTH = 206;
  public static final int PAGE_HEIGHT = 200;

  public static boolean side = false;

  private GuiArrow leftArrow, rightArrow;

  private final BookData book;

  private int page = -1;
  private ArrayList<BookElement> leftElements = new ArrayList<>();
  private ArrayList<BookElement> rightElements = new ArrayList<>();
  //TODO page cache

  public GuiBook(BookData book) {
    this.book = book;
  }

  public void drawerTransform(boolean rightSide) {
    side = rightSide;
    if (rightSide)
      GlStateManager.translate(width / 2, height / 2 - PAGE_HEIGHT / 2, 0);
    else
      GlStateManager.translate(width / 2 - PAGE_WIDTH, height / 2 - PAGE_HEIGHT / 2, 0);

  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    FontRenderer font = mc.fontRendererObj;

    GlStateManager.pushMatrix();
    GlStateManager.color(1F, 1F, 1F);

    float coverR = ((book.cover.color >> 16) & 0xff) / 255.F;
    float coverG = ((book.cover.color >> 8) & 0xff) / 255.F;
    float coverB = (book.cover.color & 0xff) / 255.F;

    TextureManager render = this.mc.renderEngine;

    if (page == -1) {
      render.bindTexture(TEX_BOOKFRONT);

      GlStateManager.color(coverR, coverG, coverB);
      drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH / 2, height / 2 - PAGE_HEIGHT / 2, 0, 0, PAGE_WIDTH, PAGE_HEIGHT, 512, 512);
      GlStateManager.color(1F, 1F, 1F);

      if (!book.cover.title.isEmpty()) {
        drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH / 2, height / 2 - PAGE_HEIGHT / 2, 0, PAGE_HEIGHT, PAGE_WIDTH, PAGE_HEIGHT, 512, 512);

        GlStateManager.pushMatrix();

        float scale = font.getStringWidth(book.cover.title) <= 67 ? 2.5F : 2F;

        GlStateManager.scale(scale, scale, 1F);
        font.drawString(book.cover.title, (width / 2) / scale + 3 - font.getStringWidth(book.cover.title) / 2, (height / 2 - font.FONT_HEIGHT / 2) / scale - 4, 0xAE8000, true);
        GlStateManager.popMatrix();
      }

      if (!book.cover.subtitle.isEmpty()) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.5F, 1.5F, 1F);
        font.drawString(book.cover.subtitle, (width / 2) / 1.5F + 7 - font.getStringWidth(book.cover.subtitle) / 2, (height / 2 + 100 - font.FONT_HEIGHT * 2) / 1.5F, 0xAE8000, true);
        GlStateManager.popMatrix();
      }
    } else {
      render.bindTexture(TEX_BOOK);

      GlStateManager.color(coverR, coverG, coverB);
      drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH, height / 2 - PAGE_HEIGHT / 2, 0, 0, PAGE_WIDTH * 2, PAGE_HEIGHT, 512, 512);

      GlStateManager.color(1F, 1F, 1F);

      if (page != 0) {
        drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH, height / 2 - PAGE_HEIGHT / 2, 0, PAGE_HEIGHT, PAGE_WIDTH, PAGE_HEIGHT, 512, 512);

        GlStateManager.pushMatrix();
        drawerTransform(false);

        for (BookElement element : leftElements) {
          GlStateManager.color(1F, 1F, 1F, 1F);
          element.draw(Mouse.getX() * this.width / this.mc.displayWidth - (width / 2 - PAGE_WIDTH), this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1 - (height / 2 - PAGE_HEIGHT / 2), partialTicks);
        }

        GlStateManager.popMatrix();
      }

      // Rebind texture as the font renderer binds its own texture
      render.bindTexture(TEX_BOOK);
      // Set color back to white
      GlStateManager.color(1F, 1F, 1F, 1F);

      if (!(page == book.fullPageCount - 1 && book.pageCount % 2 == 0)) {
        drawModalRectWithCustomSizedTexture(width / 2, height / 2 - PAGE_HEIGHT / 2, PAGE_WIDTH, PAGE_HEIGHT, PAGE_WIDTH, PAGE_HEIGHT, 512, 512);

        GlStateManager.pushMatrix();
        drawerTransform(true);

        for (BookElement element : rightElements) {
          GlStateManager.color(1F, 1F, 1F, 1F);
          element.draw(Mouse.getX() * this.width / this.mc.displayWidth - (width / 2), this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1 - (height / 2 - PAGE_HEIGHT / 2), partialTicks);
        }

        GlStateManager.popMatrix();
      }
    }

    super.drawScreen(mouseX, mouseY, partialTicks);

    GlStateManager.popMatrix();
  }

  private void buildPages() {
    leftElements.clear();
    rightElements.clear();

    if (page == -1)
      return;

    if (page == 0) {
      PageData page = book.findPage(0);

      if (page != null)
        page.content.build(rightElements);
    } else {
      PageData leftPage = book.findPage((page - 1) * 2 + 1);
      PageData rightPage = book.findPage((page - 1) * 2 + 2);

      if (leftPage != null)
        leftPage.content.build(leftElements);
      if (rightPage != null)
        rightPage.content.build(rightElements);
    }
  }

  @Override
  public void initGui() {
    super.initGui();

    leftArrow = new GuiArrow(0, -50, -50, 1);
    rightArrow = new GuiArrow(1, -50, -50, 0);

    buttonList.add(leftArrow);
    buttonList.add(rightArrow);

    buildPages();
  }

  @Override
  public void updateScreen() {
    super.updateScreen();

    leftArrow.visible = page != -1;
    rightArrow.visible = page != book.fullPageCount - 1;

    if (page == -1) {
      rightArrow.xPosition = width / 2 + 80;
    } else {
      leftArrow.xPosition = width / 2 - 190;
      rightArrow.xPosition = width / 2 + 170;
    }

    leftArrow.yPosition = height / 2 + 89;
    rightArrow.yPosition = height / 2 + 89;
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if (button == leftArrow) {
      page--;
      if (page < -1)
        page = -1;
    } else if (button == rightArrow) {
      page++;
      if (page > book.fullPageCount - 1)
        page = book.fullPageCount - 1;
    }

    buildPages();
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);

    if (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_A)
      actionPerformed(leftArrow);
    else if (keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_D)
      actionPerformed(rightArrow);
  }
}

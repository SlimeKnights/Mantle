package slimeknights.mantle.client.gui.book;

import java.io.IOException;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.gui.book.element.BookElement;

@SideOnly(Side.CLIENT)
public class GuiBook extends GuiScreen {

  private static final ResourceLocation TEX_BOOK = new ResourceLocation("mantle:textures/gui/book.png");
  private static final ResourceLocation TEX_BOOKFRONT = new ResourceLocation("mantle:textures/gui/bookfront.png");

  public static final int PAGE_PADDING = 8;
  public static final int PAGE_MARGIN = 18;

  public static final float PAGE_SCALE = 0.5F;
  public static final int PAGE_WIDTH_UNSCALED = 206;
  public static final int PAGE_HEIGHT_UNSCALED = 200;

  // For best results, make sure both PAGE_WIDTH_UNSCALED - (PAGE_PADDING + PAGE_MARGIN) * 2 and PAGE_HEIGHT_UNSCALED - (PAGE_PADDING + PAGE_MARGIN) * 2 divide evenly into PAGE_SCALE (without remainder)
  public static final int PAGE_WIDTH = (int) ((PAGE_WIDTH_UNSCALED - (PAGE_PADDING + PAGE_MARGIN) * 2) / PAGE_SCALE);
  public static final int PAGE_HEIGHT = (int) ((PAGE_HEIGHT_UNSCALED - (PAGE_PADDING + PAGE_MARGIN) * 2) / PAGE_SCALE);

  public static boolean side = false;

  private GuiArrow previousArrow, nextArrow, backArrow, indexArrow;

  public final BookData book;
  private ItemStack item;

  private int page = -1;
  private int oldPage = -2;
  private ArrayList<BookElement> leftElements = new ArrayList<>();
  private ArrayList<BookElement> rightElements = new ArrayList<>();

  public GuiBook(BookData book, @Nullable ItemStack item) {
    this.book = book;
    this.item = item;

    openPage(book.findPageNumber(BookHelper.getSavedPage(item)));
  }

  public void drawerTransform(boolean rightSide) {
    side = rightSide;
    if (rightSide)
      GlStateManager.translate(width / 2 + PAGE_PADDING + PAGE_MARGIN, height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING + PAGE_MARGIN, 0);
    else
      GlStateManager.translate(width / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING + PAGE_MARGIN, height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING + PAGE_MARGIN, 0);

  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    FontRenderer font = mc.fontRendererObj;
    GlStateManager.enableAlpha();
    GlStateManager.enableBlend();

    // The books are unreadable at Gui Scale set to small, so we'll double the scale
    if (mc.gameSettings.guiScale == 1) {
      GlStateManager.scale(2F, 2F, 2F);

      mouseX /= 2;
      mouseY /= 2;
    }

    GlStateManager.pushMatrix();
    GlStateManager.color(1F, 1F, 1F);

    float coverR = ((book.appearance.coverColor >> 16) & 0xff) / 255.F;
    float coverG = ((book.appearance.coverColor >> 8) & 0xff) / 255.F;
    float coverB = (book.appearance.coverColor & 0xff) / 255.F;

    TextureManager render = this.mc.renderEngine;

    if (page == -1) {
      render.bindTexture(TEX_BOOKFRONT);

      GlStateManager.color(coverR, coverG, coverB);
      drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, 512, 512);
      GlStateManager.color(1F, 1F, 1F);

      if (!book.appearance.title.isEmpty()) {
        drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, 512, 512);

        GlStateManager.pushMatrix();

        float scale = font.getStringWidth(book.appearance.title) <= 67 ? 2.5F : 2F;

        GlStateManager.scale(scale, scale, 1F);
        font.drawString(book.appearance.title, (width / 2) / scale + 3 - font.getStringWidth(book.appearance.title) / 2, (height / 2 - font.FONT_HEIGHT / 2) / scale - 4, 0xAE8000, true);
        GlStateManager.popMatrix();
      }

      if (!book.appearance.subtitle.isEmpty()) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.5F, 1.5F, 1F);
        font.drawString(book.appearance.subtitle, (width / 2) / 1.5F + 7 - font.getStringWidth(book.appearance.subtitle) / 2, (height / 2 + 100 - font.FONT_HEIGHT * 2) / 1.5F, 0xAE8000, true);
        GlStateManager.popMatrix();
      }
    } else {
      render.bindTexture(TEX_BOOK);

      GlStateManager.color(coverR, coverG, coverB);
      drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED * 2, PAGE_HEIGHT_UNSCALED, 512, 512);

      GlStateManager.color(1F, 1F, 1F);

      if (page != 0) {
        drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, 512, 512);

        GlStateManager.pushMatrix();
        drawerTransform(false);

        GlStateManager.scale(PAGE_SCALE, PAGE_SCALE, 1F);

        if (book.appearance.drawPageNumbers) {
          String pNum = (page - 1) * 2 + 2 + "";
          font.drawString(pNum, PAGE_WIDTH / 2 - font.getStringWidth(pNum) / 2, PAGE_HEIGHT + 15, 0xFFAAAAAA, false);
        }

        // Not foreach to prevent conmodification crashes
        for (int i = 0; i < leftElements.size(); i++) {
          BookElement element = leftElements.get(i);

          GlStateManager.color(1F, 1F, 1F, 1F);
          element.draw((int) ((Mouse.getX() * this.width / this.mc.displayWidth - (width / 2 - PAGE_WIDTH_UNSCALED) - PAGE_PADDING - PAGE_MARGIN) / PAGE_SCALE), (int) ((this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1 - (height / 2 - PAGE_HEIGHT_UNSCALED / 2) - PAGE_PADDING - PAGE_MARGIN) / PAGE_SCALE), partialTicks);
        }

        GlStateManager.popMatrix();
      }

      // Rebind texture as the font renderer binds its own texture
      render.bindTexture(TEX_BOOK);
      // Set color back to white
      GlStateManager.color(1F, 1F, 1F, 1F);

      if ((page < book.getFullPageCount() - 1 || book.getPageCount() % 2 != 0) && page < book.getFullPageCount()) {
        drawModalRectWithCustomSizedTexture(width / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, 512, 512);

        GlStateManager.pushMatrix();
        drawerTransform(true);

        GlStateManager.scale(PAGE_SCALE, PAGE_SCALE, 1F);

        if (book.appearance.drawPageNumbers) {
          String pNum = (page - 1) * 2 + 3 + "";
          font.drawString(pNum, PAGE_WIDTH / 2 - font.getStringWidth(pNum) / 2, PAGE_HEIGHT + 15, 0xFFAAAAAA, false);
        }

        // Not foreach to prevent conmodification crashes
        for (int i = 0; i < rightElements.size(); i++) {
          BookElement element = rightElements.get(i);

          GlStateManager.color(1F, 1F, 1F, 1F);
          element.draw((int) ((Mouse.getX() * this.width / this.mc.displayWidth - (width / 2) - PAGE_PADDING - PAGE_MARGIN) / PAGE_SCALE), (int) ((this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1 - (height / 2 - PAGE_HEIGHT_UNSCALED / 2) - PAGE_PADDING - PAGE_MARGIN) / PAGE_SCALE), partialTicks);
        }

        GlStateManager.popMatrix();
      }
    }

    super.drawScreen(mouseX, mouseY, partialTicks);

    GlStateManager.popMatrix();
  }

  public void openPage(int page) {
    openPage(page, false);
  }

  public void openPage(int page, boolean returner) {
    if (page < 0)
      return;

    int bookPage;
    if (page == 1)
      bookPage = 0;
    else if (page % 2 == 0)
      bookPage = (page - 1) / 2 + 1;
    else
      bookPage = (page - 2) / 2 + 1;

    if (bookPage >= -1 && bookPage < book.getFullPageCount()) {
      if (returner)
        oldPage = this.page;

      this.page = bookPage;
      buildPages();
    }
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

    for (BookElement element : leftElements)
      element.parent = this;
    for (BookElement element : rightElements)
      element.parent = this;
  }

  @Override
  public void initGui() {
    super.initGui();

    // The books are unreadable at Gui Scale set to small, so we'll double the scale, and of course half the size so that all our code still works as it should
    if (mc.gameSettings.guiScale == 1) {
      width /= 2F;
      height /= 2F;
    }

    previousArrow = new GuiArrow(0, -50, -50, 1, book.appearance.arrowColor, book.appearance.arrowColorHover);
    nextArrow = new GuiArrow(1, -50, -50, 0, book.appearance.arrowColor, book.appearance.arrowColorHover);
    backArrow = new GuiArrow(2, width / 2 - GuiArrow.WIDTH / 2, height / 2 - GuiArrow.HEIGHT / 2 + PAGE_HEIGHT / 3, 3, book.appearance.arrowColor, book.appearance.arrowColorHover);
    indexArrow = new GuiArrow(3, width / 2 - PAGE_WIDTH_UNSCALED, height / 2 - PAGE_HEIGHT_UNSCALED / 2 - 3, 3, book.appearance.arrowColor, book.appearance.arrowColorHover);

    buttonList.add(previousArrow);
    buttonList.add(nextArrow);
    buttonList.add(backArrow);
    buttonList.add(indexArrow);

    buildPages();
  }

  @Override
  public void updateScreen() {
    super.updateScreen();

    previousArrow.visible = page != -1;
    nextArrow.visible = page < book.getFullPageCount() - (book.getPageCount() % 2 != 0 ? 0 : 1);
    backArrow.visible = oldPage >= -1;

    if (page == -1) {
      nextArrow.xPosition = width / 2 + 80;
      indexArrow.visible = false;
    } else {
      previousArrow.xPosition = width / 2 - 184;
      nextArrow.xPosition = width / 2 + 165;

      indexArrow.visible = (page - 1) * 2 + 2 > book.findSection("index").getPageCount();
    }

    previousArrow.yPosition = height / 2 + 75;
    nextArrow.yPosition = height / 2 + 75;
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if (button instanceof GuiBookmark) {
      openPage(book.findPageNumber(((GuiBookmark) button).data.page));

      return;
    }

    if (button == previousArrow) {
      page--;
      if (page < -1)
        page = -1;
    } else if (button == nextArrow) {
      page++;
      if (page > book.getFullPageCount() - (book.getPageCount() % 2 != 0 ? 0 : 1))
        page = book.getFullPageCount() - 1;
    } else if (button == backArrow) {
      if (oldPage >= -1)
        page = oldPage;
    } else if (button == indexArrow) {
      openPage(book.findPageNumber("index.page1"));
    }

    oldPage = -2;
    buildPages();
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);

    if (keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_A)
      actionPerformed(previousArrow);
    else if (keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_D)
      actionPerformed(nextArrow);
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);

    boolean right = false;

    mouseX = (int) ((Mouse.getX() * this.width / this.mc.displayWidth - (width / 2 - PAGE_WIDTH_UNSCALED) - PAGE_PADDING - PAGE_MARGIN) / PAGE_SCALE);
    mouseY = (int) ((this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1 - (height / 2 - PAGE_HEIGHT_UNSCALED / 2) - PAGE_PADDING - PAGE_MARGIN) / PAGE_SCALE);

    if (mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING) / PAGE_SCALE) {
      mouseX = (int) ((Mouse.getX() * this.width / this.mc.displayWidth - (width / 2) - PAGE_PADDING - PAGE_MARGIN) / PAGE_SCALE);
      right = true;
    }

    // Not foreach to prevent conmodification crashes
    for (int i = 0; right ? i < rightElements.size() : i < leftElements.size(); i++) {
      BookElement element = right ? rightElements.get(i) : leftElements.get(i);

      element.mouseClicked(mouseX, mouseY, mouseButton);
    }
  }

  @Override
  public void onGuiClosed() {
    PageData page = this.page == 0 ? book.findPage(0) : book.findPage((this.page - 1) * 2 + 1);

    if (page == null)
      page = book.findPage((this.page - 1) * 2 + 2);

    if(this.page == -1)
      BookLoader.updateSavedPage(mc.thePlayer, item, "");
    else if (page != null)
      BookLoader.updateSavedPage(mc.thePlayer, item, page.parent.name + "." + page.name);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
}

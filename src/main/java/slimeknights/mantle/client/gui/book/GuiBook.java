package slimeknights.mantle.client.gui.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.stats.StatisticsManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.Nullable;

import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.element.ItemStackData;
import slimeknights.mantle.client.gui.book.element.BookElement;

import static slimeknights.mantle.client.gui.book.Textures.TEX_BOOK;
import static slimeknights.mantle.client.gui.book.Textures.TEX_BOOKFRONT;

@SideOnly(Side.CLIENT)
public class GuiBook extends GuiScreen implements IProgressMeter {

  public static boolean debug = false;

  public static final int TEX_SIZE = 512;

  public static int PAGE_MARGIN = 8;

  public static int PAGE_PADDING_TOP = 4;
  public static int PAGE_PADDING_BOT = 4;
  public static int PAGE_PADDING_LEFT = 8;
  public static int PAGE_PADDING_RIGHT = 0;

  public static float PAGE_SCALE = 1f;
  public static int PAGE_WIDTH_UNSCALED = 206;
  public static int PAGE_HEIGHT_UNSCALED = 200;

  // For best results, make sure both PAGE_WIDTH_UNSCALED - (PAGE_PADDING + PAGE_MARGIN) * 2 and PAGE_HEIGHT_UNSCALED - (PAGE_PADDING + PAGE_MARGIN) * 2 divide evenly into PAGE_SCALE (without remainder)
  public static int PAGE_WIDTH;
  public static int PAGE_HEIGHT;

  static{
    init(); // initializes page width and height
  }

  private boolean loadingAchievements = true;
  private GuiArrow previousArrow, nextArrow, backArrow, indexArrow;

  public final BookData book;
  private ItemStack item;

  private int page = -1;
  private int oldPage = -2;
  private ArrayList<BookElement> leftElements = new ArrayList<>();
  private ArrayList<BookElement> rightElements = new ArrayList<>();

  public StatisticsManager statisticsManager;

  public static void init() {
    PAGE_WIDTH = (int) ((PAGE_WIDTH_UNSCALED - (PAGE_PADDING_LEFT + PAGE_PADDING_RIGHT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);
    PAGE_HEIGHT = (int) ((PAGE_HEIGHT_UNSCALED - (PAGE_PADDING_TOP + PAGE_PADDING_BOT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);
  }

  public GuiBook(BookData book, StatisticsManager statisticsManager, @Nullable ItemStack item) {
    this.book = book;
    this.item = item;

    this.statisticsManager = statisticsManager;
    this.mc = Minecraft.getMinecraft();
    this.fontRendererObj = mc.fontRendererObj;
    init();
  }

  public void drawerTransform(boolean rightSide) {
    if(rightSide) {
      GlStateManager.translate(width / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN, height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    } else {
      GlStateManager.translate(width / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN, height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    }
  }

  // offset to the left edge of the left/right side
  protected float leftOffset(boolean rightSide) {
    if(rightSide) {
      // from center: go padding + margin to the right
      return width / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN;
    } else {
      // from center: go page width left, then right with padding and margin
      return width / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN;
    }
  }

  protected float topOffset() {
    return height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN;
  }

  protected int getMouseX(boolean rightSide) {
    return (int) ((Mouse.getX() * this.width / this.mc.displayWidth - leftOffset(rightSide)) / PAGE_SCALE);
  }

  protected int getMouseY() {
    return (int) ((this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1 - topOffset()) / PAGE_SCALE);
  }

  @Override
  @SuppressWarnings("ForLoopReplaceableByForEach")
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    init();
    FontRenderer fontRenderer = book.fontRenderer;
    if(fontRenderer == null) {
      fontRenderer = mc.fontRendererObj;
    }

    if(loadingAchievements) {
      this.drawDefaultBackground();

      this.drawCenteredString(this.fontRendererObj, I18n
          .format("multiplayer.downloadingStats"), this.width / 2, this.height / 2, 16777215);
      this.drawCenteredString(this.fontRendererObj, LOADING_STRINGS[(int) (Minecraft
                                                                               .getSystemTime() / 150L % (long) LOADING_STRINGS.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2, 16777215);

      return;
    }

    if(debug) {
      drawRect(0, 0, fontRenderer.getStringWidth("DEBUG") + 4, fontRenderer.FONT_HEIGHT + 4, 0x55000000);
      fontRenderer.drawString("DEBUG", 2, 2, 0xFFFFFFFF);
    }

    GlStateManager.enableAlpha();
    GlStateManager.enableBlend();

    // The books are unreadable at Gui Scale set to small, so we'll double the scale
    /*
    if(mc.gameSettings.guiScale == 1) {
      float f = 1.5f;
      GlStateManager.scale(f, f, 1);

      float ox = this.width/6;
      float oy = this.height/6;

      mouseX = (int)((float)mouseX / f);
      mouseY = (int)((float)mouseY / f);

      GlStateManager.translate(ox, oy, 0);
    }
    else if(mc.gameSettings.guiScale == 2) {
      float f = 3f/2f;
      GlStateManager.scale(f, f, 1);

      float ox = -this.width/6;
      float oy = -this.height/6;

      mouseX = (int)(((float)mouseX - ox)/f);
      mouseY = (int)(((float)mouseY - oy)/f);

      GlStateManager.translate(ox, oy, 0);
    }
*/
    GlStateManager.pushMatrix();
    GlStateManager.color(1F, 1F, 1F);

    float coverR = ((book.appearance.coverColor >> 16) & 0xff) / 255.F;
    float coverG = ((book.appearance.coverColor >> 8) & 0xff) / 255.F;
    float coverB = (book.appearance.coverColor & 0xff) / 255.F;

    TextureManager render = this.mc.renderEngine;

    if(page == -1) {
      render.bindTexture(TEX_BOOKFRONT);
      RenderHelper.disableStandardItemLighting();

      GlStateManager.color(coverR, coverG, coverB);
      drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);
      GlStateManager.color(1F, 1F, 1F);

      if(!book.appearance.title.isEmpty()) {
        drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        GlStateManager.pushMatrix();

        float scale = fontRenderer.getStringWidth(book.appearance.title) <= 67 ? 2.5F : 2F;

        GlStateManager.scale(scale, scale, 1F);
        fontRenderer.drawString(book.appearance.title, (width / 2) / scale + 3 - fontRenderer
                                                                                     .getStringWidth(book.appearance.title) / 2, (height / 2 - fontRenderer.FONT_HEIGHT / 2) / scale - 4, 0xAE8000, true);
        GlStateManager.popMatrix();
      }

      if(!book.appearance.subtitle.isEmpty()) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.5F, 1.5F, 1F);
        fontRenderer.drawString(book.appearance.subtitle, (width / 2) / 1.5F + 7 - fontRenderer
                                                                                       .getStringWidth(book.appearance.subtitle) / 2, (height / 2 + 100 - fontRenderer.FONT_HEIGHT * 2) / 1.5F, 0xAE8000, true);
        GlStateManager.popMatrix();
      }
    } else {
      render.bindTexture(TEX_BOOK);
      RenderHelper.disableStandardItemLighting();

      GlStateManager.color(coverR, coverG, coverB);
      drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED * 2, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

      GlStateManager.color(1F, 1F, 1F);

      if(page != 0) {
        drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        GlStateManager.pushMatrix();
        drawerTransform(false);

        GlStateManager.scale(PAGE_SCALE, PAGE_SCALE, 1F);

        if(book.appearance.drawPageNumbers) {
          String pNum = (page - 1) * 2 + 2 + "";
          fontRenderer.drawString(pNum, PAGE_WIDTH / 2 - fontRenderer.getStringWidth(pNum) / 2, PAGE_HEIGHT - 10, 0xFFAAAAAA, false);
        }

        int mX = getMouseX(false);
        int mY = getMouseY();

        // Not foreach to prevent conmodification crashes
        for(int i = 0; i < leftElements.size(); i++) {
          BookElement element = leftElements.get(i);

          GlStateManager.color(1F, 1F, 1F, 1F);
          element.draw(mX, mY, partialTicks, fontRenderer);
        }

        // Not foreach to prevent conmodification crashes
        for(int i = 0; i < leftElements.size(); i++) {
          BookElement element = leftElements.get(i);

          GlStateManager.color(1F, 1F, 1F, 1F);
          element.drawOverlay(mX, mY, partialTicks, fontRenderer);
        }

        GlStateManager.popMatrix();
      }

      // Rebind texture as the font renderer binds its own texture
      render.bindTexture(TEX_BOOK);
      // Set color back to white
      GlStateManager.color(1F, 1F, 1F, 1F);
      RenderHelper.disableStandardItemLighting();

      if((page < book.getFullPageCount(statisticsManager) - 1 || book.getPageCount(statisticsManager) % 2 != 0) && page < book
          .getFullPageCount(statisticsManager)) {
        drawModalRectWithCustomSizedTexture(width / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        GlStateManager.pushMatrix();
        drawerTransform(true);

        GlStateManager.scale(PAGE_SCALE, PAGE_SCALE, 1F);

        if(book.appearance.drawPageNumbers) {
          String pNum = (page - 1) * 2 + 3 + "";
          fontRenderer.drawString(pNum, PAGE_WIDTH / 2 - fontRenderer.getStringWidth(pNum) / 2, PAGE_HEIGHT - 10, 0xFFAAAAAA, false);
        }

        int mX = getMouseX(true);
        int mY = getMouseY();

        // Not foreach to prevent conmodification crashes
        for(int i = 0; i < rightElements.size(); i++) {
          BookElement element = rightElements.get(i);

          GlStateManager.color(1F, 1F, 1F, 1F);
          element.draw(mX, mY, partialTicks, fontRenderer);
        }

        // Not foreach to prevent conmodification crashes
        for(int i = 0; i < rightElements.size(); i++) {
          BookElement element = rightElements.get(i);

          GlStateManager.color(1F, 1F, 1F, 1F);
          element.drawOverlay(mX, mY, partialTicks, fontRenderer);
        }

        GlStateManager.popMatrix();
      }
    }

    super.drawScreen(mouseX, mouseY, partialTicks);

    GlStateManager.popMatrix();
  }

  public int openPage(int page) {
    return openPage(page, false);
  }

  public int openPage(int page, boolean returner) {
    if(loadingAchievements) {
      return -1;
    }

    if(page < 0) {
      return -1;
    }

    int bookPage;
    if(page == 1) {
      bookPage = 0;
    } else if(page % 2 == 0) {
      bookPage = (page - 1) / 2 + 1;
    } else {
      bookPage = (page - 2) / 2 + 1;
    }

    if(bookPage >= -1 && bookPage < book.getFullPageCount(statisticsManager)) {
      if(returner) {
        oldPage = this.page;
      }

      _setPage(bookPage);
    }

    return page % 2 == 0 ? 0 : 1;
  }

  public void _setPage(int page) {
    this.page = page;
    buildPages();
  }

  public int getPage(int side) {
    if(page == 0 && side == 0) {
      return -1;
    } else if(page == 0 && side == 1) {
      return 0;
    } else if(side == 0) {
      return (page - 1) * 2 + 1;
    } else if(side == 1) {
      return (page - 2) * 2 + 2;
    } else {
      return -1;
    }
  }

  public int getPage_() {
    return page;
  }

  public ArrayList<BookElement> getElements(int side) {
    return side == 0 ? leftElements : side == 1 ? rightElements : null;
  }

  public void openCover() {
    if(loadingAchievements) {
      return;
    }

    _setPage(-1);

    this.leftElements.clear();
    this.rightElements.clear();
    buildPages();
  }

  public void itemClicked(ItemStack item) {
    StringActionProcessor.process(book.getItemAction(ItemStackData.getItemStackData(item, true)), this);
  }

  private void buildPages() {
    if(loadingAchievements) {
      return;
    }

    leftElements.clear();
    rightElements.clear();

    if(page == -1) {
      return;
    }

    if(page == 0) {
      PageData page = book.findPage(0, statisticsManager);

      if(page != null) {
        page.content.build(book, rightElements, false);
      }
    } else {
      PageData leftPage = book.findPage((page - 1) * 2 + 1, statisticsManager);
      PageData rightPage = book.findPage((page - 1) * 2 + 2, statisticsManager);

      if(leftPage != null) {
        leftPage.content.build(book, leftElements, false);
      }
      if(rightPage != null) {
        rightPage.content.build(book, rightElements, true);
      }
    }

    for(BookElement element : leftElements) {
      element.parent = this;
    }
    for(BookElement element : rightElements) {
      element.parent = this;
    }
  }

  @Override
  public void initGui() {
    super.initGui();

    if(loadingAchievements) {
      this.mc.getConnection().sendPacket(new CPacketClientStatus((CPacketClientStatus.State.REQUEST_STATS)));
      return;
    }

    // The books are unreadable at Gui Scale set to small, so we'll double the scale, and of course half the size so that all our code still works as it should
    /*
    if(mc.gameSettings.guiScale == 1) {
      width /= 2F;
      height /= 2F;
    }*/

    previousArrow = new GuiArrow(0, -50, -50, GuiArrow.ArrowType.PREV, book.appearance.arrowColor, book.appearance.arrowColorHover);
    nextArrow = new GuiArrow(1, -50, -50, GuiArrow.ArrowType.NEXT, book.appearance.arrowColor, book.appearance.arrowColorHover);
    backArrow = new GuiArrow(2, width / 2 - GuiArrow.WIDTH / 2, height / 2 + GuiArrow.HEIGHT / 2 + PAGE_HEIGHT/2, GuiArrow.ArrowType.LEFT, book.appearance.arrowColor, book.appearance.arrowColorHover);
    indexArrow = new GuiArrow(3, width / 2 - PAGE_WIDTH_UNSCALED - GuiArrow.WIDTH / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, GuiArrow.ArrowType.BACK_UP, book.appearance.arrowColor, book.appearance.arrowColorHover);

    buttonList.clear();
    buttonList.add(previousArrow);
    buttonList.add(nextArrow);
    buttonList.add(backArrow);
    buttonList.add(indexArrow);

    buildPages();
  }

  @Override
  public void updateScreen() {
    super.updateScreen();

    if(loadingAchievements) {
      return;
    }

    previousArrow.visible = page != -1;
    nextArrow.visible = page < book.getFullPageCount(statisticsManager) - (book.getPageCount(statisticsManager) % 2 != 0 ? 0 : 1);
    backArrow.visible = oldPage >= -1;

    if(page == -1) {
      nextArrow.xPosition = width / 2 + 80;
      indexArrow.visible = false;
    } else {
      previousArrow.xPosition = width / 2 - 184;
      nextArrow.xPosition = width / 2 + 165;

      indexArrow.visible = book.findSection("index") != null && (page - 1) * 2 + 2 > book.findSection("index")
                                                                                         .getPageCount();
    }

    previousArrow.yPosition = height / 2 + 75;
    nextArrow.yPosition = height / 2 + 75;
  }

  @Override
  public void doneLoading() {
    loadingAchievements = false;

    initGui();
    openPage(book.findPageNumber(BookHelper.getSavedPage(item), statisticsManager));
  }

  @Override
  public void actionPerformed(GuiButton button) {
    if(loadingAchievements) {
      return;
    }

    if(button instanceof GuiBookmark) {
      openPage(book.findPageNumber(((GuiBookmark) button).data.page, statisticsManager));

      return;
    }

    if(button == previousArrow) {
      page--;
      if(page < -1) {
        page = -1;
      }
    } else if(button == nextArrow) {
      page++;
      if(page > book.getFullPageCount(statisticsManager) - (book.getPageCount(statisticsManager) % 2 != 0 ? 0 : 1)) {
        page = book.getFullPageCount(statisticsManager) - 1;
      }
    } else if(button == backArrow) {
      if(oldPage >= -1) {
        page = oldPage;
      }
    } else if(button == indexArrow) {
      openPage(book.findPageNumber("index.page1"));
    }

    oldPage = -2;
    buildPages();
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    super.keyTyped(typedChar, keyCode);

    if(loadingAchievements) {
      return;
    }

    switch(keyCode) {
      case Keyboard.KEY_LEFT:
      case Keyboard.KEY_A:
        actionPerformed(previousArrow);
        break;
      case Keyboard.KEY_RIGHT:
      case Keyboard.KEY_D:
        actionPerformed(nextArrow);
        break;
      case Keyboard.KEY_F3:
        debug = !debug;
        break;
    }
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    if(loadingAchievements) {
      return;
    }

    super.mouseClicked(mouseX, mouseY, mouseButton);

    boolean right = false;

    mouseX = getMouseX(false);
    mouseY = getMouseY();

    if(mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = getMouseX(true);
      right = true;
    }

    // Not foreach to prevent conmodification crashes
    for(int i = 0; right ? i < rightElements.size() : i < leftElements.size(); i++) {
      BookElement element = right ? rightElements.get(i) : leftElements.get(i);

      element.mouseClicked(mouseX, mouseY, mouseButton);
    }
  }

  @Override
  public void onGuiClosed() {
    if(loadingAchievements) {
      return;
    }
    if(mc.thePlayer == null) {
      return;
    }

    PageData page = this.page == 0 ? book.findPage(0, statisticsManager) : book.findPage((this.page - 1) * 2 + 1, statisticsManager);

    if(page == null) {
      page = book.findPage((this.page - 1) * 2 + 2, statisticsManager);
    }

    if(this.page == -1) {
      BookLoader.updateSavedPage(mc.thePlayer, item, "");
    } else if(page != null && page.parent != null) {
      BookLoader.updateSavedPage(mc.thePlayer, item, page.parent.name + "." + page.name);
    }
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
}

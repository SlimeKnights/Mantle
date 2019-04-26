package slimeknights.mantle.client.gui.book;

import com.google.common.collect.ImmutableList;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.multiplayer.ClientAdvancementManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.element.ItemStackData;
import slimeknights.mantle.client.gui.book.element.BookElement;

import static slimeknights.mantle.client.gui.book.Textures.TEX_BOOK;
import static slimeknights.mantle.client.gui.book.Textures.TEX_BOOKFRONT;

@OnlyIn(Dist.CLIENT)
public class GuiBook extends GuiScreen {

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

  private GuiArrow previousArrow, nextArrow, backArrow, indexArrow;

  public final BookData book;
  private ItemStack item;

  private int page = -1;
  private int oldPage = -2;
  private ArrayList<BookElement> leftElements = new ArrayList<>();
  private ArrayList<BookElement> rightElements = new ArrayList<>();

  public AdvancementCache advancementCache;

  public static void init() {
    PAGE_WIDTH = (int) ((PAGE_WIDTH_UNSCALED - (PAGE_PADDING_LEFT + PAGE_PADDING_RIGHT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);
    PAGE_HEIGHT = (int) ((PAGE_HEIGHT_UNSCALED - (PAGE_PADDING_TOP + PAGE_PADDING_BOT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);
  }

  public GuiBook(BookData book, @Nullable ItemStack item) {
    this.book = book;
    this.item = item;

    this.mc = Minecraft.getInstance();
    this.fontRenderer = mc.fontRenderer;
    init();

    advancementCache = new AdvancementCache();
    this.mc.player.connection.getAdvancementManager().setListener(advancementCache);

    openPage(book.findPageNumber(BookHelper.getSavedPage(item), advancementCache));
  }

  public void drawerTransform(boolean rightSide) {
    if(rightSide) {
      GlStateManager.translatef(width / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN, height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    } else {
      GlStateManager.translatef(width / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN, height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
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
    return (int) ((Minecraft.getInstance().mouseHelper.getMouseX() * this.width / this.mc.mainWindow.getFramebufferWidth() - leftOffset(rightSide)) / PAGE_SCALE);
  }

  protected int getMouseY() {
    return (int) ((this.height - Minecraft.getInstance().mouseHelper.getMouseY() * this.height / this.mc.mainWindow.getFramebufferWidth() - 1 - topOffset()) / PAGE_SCALE);
  }

  @Override
  @SuppressWarnings("ForLoopReplaceableByForEach")
  public void render(int mouseX, int mouseY, float partialTicks) {
    init();
    FontRenderer fontRenderer = book.fontRenderer;
    if(fontRenderer == null) {
      fontRenderer = mc.fontRenderer;
    }

    if(debug) {
      drawRect(0, 0, fontRenderer.getStringWidth("DEBUG") + 4, fontRenderer.FONT_HEIGHT + 4, 0x55000000);
      fontRenderer.drawString("DEBUG", 2, 2, 0xFFFFFFFF);
    }

    GlStateManager.enableAlphaTest();
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
    GlStateManager.color3f(1F, 1F, 1F);

    float coverR = ((book.appearance.coverColor >> 16) & 0xff) / 255.F;
    float coverG = ((book.appearance.coverColor >> 8) & 0xff) / 255.F;
    float coverB = (book.appearance.coverColor & 0xff) / 255.F;

    TextureManager render = this.mc.textureManager;

    if(page == -1) {
      render.bindTexture(TEX_BOOKFRONT);
      RenderHelper.disableStandardItemLighting();

      GlStateManager.color3f(coverR, coverG, coverB);
      drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);
      GlStateManager.color3f(1F, 1F, 1F);

      if(!book.appearance.title.isEmpty()) {
        drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        GlStateManager.pushMatrix();

        float scale = fontRenderer.getStringWidth(book.appearance.title) <= 67 ? 2.5F : 2F;

        GlStateManager.scalef(scale, scale, 1F);
        fontRenderer.drawStringWithShadow(book.appearance.title, (width / 2) / scale + 3 - fontRenderer
                                                                                     .getStringWidth(book.appearance.title) / 2, (height / 2 - fontRenderer.FONT_HEIGHT / 2) / scale - 4, 0xAE8000);
        GlStateManager.popMatrix();
      }

      if(!book.appearance.subtitle.isEmpty()) {
        GlStateManager.pushMatrix();
        GlStateManager.scalef(1.5F, 1.5F, 1F);
        fontRenderer.drawStringWithShadow(book.appearance.subtitle, (width / 2) / 1.5F + 7 - fontRenderer
                                                                                       .getStringWidth(book.appearance.subtitle) / 2, (height / 2 + 100 - fontRenderer.FONT_HEIGHT * 2) / 1.5F, 0xAE8000);
        GlStateManager.popMatrix();
      }
    } else {
      render.bindTexture(TEX_BOOK);
      RenderHelper.disableStandardItemLighting();

      GlStateManager.color3f(coverR, coverG, coverB);
      drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED * 2, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

      GlStateManager.color3f(1F, 1F, 1F);

      if(page != 0) {
        drawModalRectWithCustomSizedTexture(width / 2 - PAGE_WIDTH_UNSCALED, height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        GlStateManager.pushMatrix();
        drawerTransform(false);

        GlStateManager.scalef(PAGE_SCALE, PAGE_SCALE, 1F);

        if(book.appearance.drawPageNumbers) {
          String pNum = (page - 1) * 2 + 2 + "";
          fontRenderer.drawString(pNum, PAGE_WIDTH / 2 - fontRenderer.getStringWidth(pNum) / 2, PAGE_HEIGHT - 10, 0xFFAAAAAA);
        }

        int mX = getMouseX(false);
        int mY = getMouseY();

        // Not foreach to prevent conmodification crashes
        for(int i = 0; i < leftElements.size(); i++) {
          BookElement element = leftElements.get(i);

          GlStateManager.color4f(1F, 1F, 1F, 1F);
          element.draw(mX, mY, partialTicks, fontRenderer);
        }

        // Not foreach to prevent conmodification crashes
        for(int i = 0; i < leftElements.size(); i++) {
          BookElement element = leftElements.get(i);

          GlStateManager.color4f(1F, 1F, 1F, 1F);
          element.drawOverlay(mX, mY, partialTicks, fontRenderer);
        }

        GlStateManager.popMatrix();
      }

      // Rebind texture as the font renderer binds its own texture
      render.bindTexture(TEX_BOOK);
      // Set color back to white
      GlStateManager.color4f(1F, 1F, 1F, 1F);
      RenderHelper.disableStandardItemLighting();

      int fullPageCount = book.getFullPageCount(advancementCache);
      if(page < fullPageCount - 1 || book.getPageCount(advancementCache) % 2 != 0) {
        drawModalRectWithCustomSizedTexture(width / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        GlStateManager.pushMatrix();
        drawerTransform(true);

        GlStateManager.scalef(PAGE_SCALE, PAGE_SCALE, 1F);

        if(book.appearance.drawPageNumbers) {
          String pNum = (page - 1) * 2 + 3 + "";
          fontRenderer.drawString(pNum, PAGE_WIDTH / 2 - fontRenderer.getStringWidth(pNum) / 2, PAGE_HEIGHT - 10, 0xFFAAAAAA);
        }

        int mX = getMouseX(true);
        int mY = getMouseY();

        // Not foreach to prevent conmodification crashes
        for(int i = 0; i < rightElements.size(); i++) {
          BookElement element = rightElements.get(i);

          GlStateManager.color4f(1F, 1F, 1F, 1F);
          element.draw(mX, mY, partialTicks, fontRenderer);
        }

        // Not foreach to prevent conmodification crashes
        for(int i = 0; i < rightElements.size(); i++) {
          BookElement element = rightElements.get(i);

          GlStateManager.color4f(1F, 1F, 1F, 1F);
          element.drawOverlay(mX, mY, partialTicks, fontRenderer);
        }

        GlStateManager.popMatrix();
      }
    }

    super.render(mouseX, mouseY, partialTicks);

    GlStateManager.popMatrix();
  }

  public int openPage(int page) {
    return openPage(page, false);
  }

  public int openPage(int page, boolean returner) {
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

    if(bookPage >= -1 && bookPage < book.getFullPageCount(advancementCache)) {
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
    _setPage(-1);

    this.leftElements.clear();
    this.rightElements.clear();
    buildPages();
  }

  public void itemClicked(ItemStack item) {
    StringActionProcessor.process(book.getItemAction(ItemStackData.getItemStackData(item, true)), this);
  }

  private void buildPages() {
    leftElements.clear();
    rightElements.clear();

    if(page == -1) {
      return;
    }

    if(page == 0) {
      PageData page = book.findPage(0, advancementCache);

      if(page != null) {
        page.content.build(book, rightElements, false);
      }
    } else {
      PageData leftPage = book.findPage((page - 1) * 2 + 1, advancementCache);
      PageData rightPage = book.findPage((page - 1) * 2 + 2, advancementCache);

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

    // The books are unreadable at Gui Scale set to small, so we'll double the scale, and of course half the size so that all our code still works as it should
    /*
    if(mc.gameSettings.guiScale == 1) {
      width /= 2F;
      height /= 2F;
    }*/

    previousArrow = new GuiArrow(0, -50, -50, GuiArrow.ArrowType.PREV, book.appearance.arrowColor, book.appearance.arrowColorHover) {
      @Override
      public void onClick(double mouseX, double mouseY) {
        page--;
        if(page < -1) {
          page = -1;
        }

        oldPage = -2;
        buildPages();
      }
    };

    nextArrow = new GuiArrow(1, -50, -50, GuiArrow.ArrowType.NEXT, book.appearance.arrowColor, book.appearance.arrowColorHover) {
      @Override
      public void onClick(double mouseX, double mouseY) {
        page++;
        int fullPageCount = book.getFullPageCount(advancementCache);
        if(page >= fullPageCount) {
          page = fullPageCount - 1;
        }
        oldPage = -2;
        buildPages();
      }
    };

    backArrow = new GuiArrow(2, width / 2 - GuiArrow.WIDTH / 2, height / 2 + GuiArrow.HEIGHT / 2 + PAGE_HEIGHT/2, GuiArrow.ArrowType.LEFT, book.appearance.arrowColor, book.appearance.arrowColorHover) {
      @Override
      public void onClick(double mouseX, double mouseY) {
        if(oldPage >= -1) {
          page = oldPage;
        }

        oldPage = -2;
        buildPages();
      }
    };

    indexArrow = new GuiArrow(3, width / 2 - PAGE_WIDTH_UNSCALED - GuiArrow.WIDTH / 2, height / 2 - PAGE_HEIGHT_UNSCALED / 2, GuiArrow.ArrowType.BACK_UP, book.appearance.arrowColor, book.appearance.arrowColorHover) {
      @Override
      public void onClick(double mouseX, double mouseY) {
        openPage(book.findPageNumber("index.page1"));

        oldPage = -2;
        buildPages();
      }
    };

    buttons.clear();
    buttons.add(previousArrow);
    buttons.add(nextArrow);
    buttons.add(backArrow);
    buttons.add(indexArrow);

    buildPages();
  }

  @Override
  public void tick() {
    super.tick();

    previousArrow.visible = page != -1;
    nextArrow.visible = page + 1 < book.getFullPageCount(advancementCache);
    backArrow.visible = oldPage >= -1;

    if(page == -1) {
      nextArrow.x = width / 2 + 80;
      indexArrow.visible = false;
    } else {
      previousArrow.x = width / 2 - 184;
      nextArrow.x = width / 2 + 165;

      indexArrow.visible = book.findSection("index") != null && (page - 1) * 2 + 2 > book.findSection("index")
                                                                                         .getPageCount();
    }

    previousArrow.y = height / 2 + 75;
    nextArrow.y = height / 2 + 75;
  }

  /*@Override
  TODO: REMOVE
  public void actionPerformed(GuiButton button) {
    if(button instanceof GuiBookmark) {
      openPage(book.findPageNumber(((GuiBookmark) button).data.page, advancementCache));

      return;
    }
  }*/

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    super.keyPressed(keyCode, scanCode, modifiers);

    switch(keyCode) {
      case GLFW.GLFW_KEY_LEFT:
      case GLFW.GLFW_KEY_A:
        page--;
        if(page < -1) {
          page = -1;
        }

        oldPage = -2;
        buildPages();
        return true;
      case GLFW.GLFW_KEY_RIGHT:
      case GLFW.GLFW_KEY_D:
        page++;
        int fullPageCount = book.getFullPageCount(advancementCache);
        if(page >= fullPageCount) {
          page = fullPageCount - 1;
        }
        oldPage = -2;
        buildPages();
        return true;
      case GLFW.GLFW_KEY_F3:
        debug = !debug;
        return true;
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean mouseScrolled(double scrollDelta) {

    if (scrollDelta < 0.0D) {
      page++;
      int fullPageCount = book.getFullPageCount(advancementCache);
      if(page >= fullPageCount) {
        page = fullPageCount - 1;
      }
      oldPage = -2;
      buildPages();

      return true;
    }
    else if (scrollDelta > 0.0D) {
      page--;
      if(page < -1) {
        page = -1;
      }

      oldPage = -2;
      buildPages();

      return true;
    }

    return super.mouseScrolled(scrollDelta);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    boolean right = false;

    mouseX = getMouseX(false);
    mouseY = getMouseY();

    if(mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = getMouseX(true);
      right = true;
    }

    // Not foreach to prevent conmodification crashes
    int oldPage = page;
    List<BookElement> elementList = ImmutableList.copyOf(right ? rightElements: leftElements);
    for(BookElement element : elementList) {
      element.mouseClicked(mouseX, mouseY, mouseButton);
      // if we changed page stop so we don't act on the new page
      if(page != oldPage) {
        return true;
      }
    }

    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
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
      element.mouseReleased(mouseX, mouseY, mouseButton);
    }
    return super.mouseReleased(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLaslick, double unknown) {
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
      element.mouseClickMove(mouseX, mouseY, clickedMouseButton);
    }

    return true;
  }

  @Override
  public void onGuiClosed() {
    if(mc.player == null) {
      return;
    }

    PageData page = this.page == 0 ? book.findPage(0, advancementCache) : book.findPage((this.page - 1) * 2 + 1, advancementCache);

    if(page == null) {
      page = book.findPage((this.page - 1) * 2 + 2, advancementCache);
    }

    if(this.page == -1) {
      BookLoader.updateSavedPage(mc.player, item, "");
    } else if(page != null && page.parent != null) {
      BookLoader.updateSavedPage(mc.player, item, page.parent.name + "." + page.name);
    }
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  public class AdvancementCache implements ClientAdvancementManager.IListener {
    private HashMap<Advancement, AdvancementProgress> progress = new HashMap<>();
    private HashMap<ResourceLocation, Advancement> nameCache = new HashMap<>();

    public AdvancementProgress getProgress(String id){
      return getProgress(getAdvancement(id));
    }

    public AdvancementProgress getProgress(Advancement advancement){
      return progress.get(advancement);
    }

    public Advancement getAdvancement(String id){
      return nameCache.get(new ResourceLocation(id));
    }

    @Override
    public void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress advancementProgress) {
      progress.put(advancement, advancementProgress);
    }

    @Override
    public void setSelectedTab(@Nullable Advancement advancement) {
      // noop
    }

    @Override
    public void rootAdvancementAdded(Advancement advancement) {
      nameCache.put(advancement.getId(), advancement);
    }

    @Override
    public void rootAdvancementRemoved(Advancement advancement) {
      progress.remove(advancement);
      nameCache.remove(advancement.getId());
    }

    @Override
    public void nonRootAdvancementAdded(Advancement advancement) {
      nameCache.put(advancement.getId(), advancement);
    }

    @Override
    public void nonRootAdvancementRemoved(Advancement advancement) {
      progress.remove(advancement);
      nameCache.remove(advancement.getId());
    }

    @Override
    public void advancementsCleared() {
      progress.clear();
      nameCache.clear();
    }
  }
}

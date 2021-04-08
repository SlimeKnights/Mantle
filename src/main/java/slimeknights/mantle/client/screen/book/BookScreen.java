package slimeknights.mantle.client.screen.book;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.action.StringActionProcessor;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.element.ItemStackData;
import slimeknights.mantle.client.screen.book.element.BookElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static slimeknights.mantle.client.screen.book.Textures.TEX_BOOK;
import static slimeknights.mantle.client.screen.book.Textures.TEX_BOOKFRONT;

@Environment(EnvType.CLIENT)
public class BookScreen extends Screen {

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

  static {
    initWidthsAndHeights(); // initializes page width and height
  }

  private ArrowButton previousArrow, nextArrow, backArrow, indexArrow;

  public final BookData book;
  private ItemStack item;

  private int page = -1;
  private int oldPage = -2;
  private ArrayList<BookElement> leftElements = new ArrayList<>();
  private ArrayList<BookElement> rightElements = new ArrayList<>();

  public AdvancementCache advancementCache;

  //TODO: new name as vanilla now uses init
  public static void initWidthsAndHeights() {
    PAGE_WIDTH = (int) ((PAGE_WIDTH_UNSCALED - (PAGE_PADDING_LEFT + PAGE_PADDING_RIGHT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);
    PAGE_HEIGHT = (int) ((PAGE_HEIGHT_UNSCALED - (PAGE_PADDING_TOP + PAGE_PADDING_BOT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);
  }

  public BookScreen(Text title, BookData book, @Nullable ItemStack item) {
    super(title);
    this.book = book;
    this.item = item;

    this.client = MinecraftClient.getInstance();
    this.textRenderer = this.client.textRenderer;

    initWidthsAndHeights();

    this.advancementCache = new AdvancementCache();
    this.client.player.networkHandler.getAdvancementHandler().setListener(this.advancementCache);

    this.openPage(book.findPageNumber(BookHelper.getSavedPage(item), this.advancementCache));
  }

  @Override
  @SuppressWarnings("ForLoopReplaceableByForEach")
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    initWidthsAndHeights();
    TextRenderer fontRenderer = this.book.fontRenderer;
    if (fontRenderer == null) {
      fontRenderer = this.client.textRenderer;
    }

    if (debug) {
      fill(matrixStack, 0, 0, fontRenderer.getWidth("DEBUG") + 4, fontRenderer.fontHeight + 4, 0x55000000);
      fontRenderer.draw(matrixStack, "DEBUG", 2, 2, 0xFFFFFFFF);
    }

    RenderSystem.enableAlphaTest();
    RenderSystem.enableBlend();

    // The books are unreadable at Gui Scale set to small, so we'll double the scale
    RenderSystem.pushMatrix();
    RenderSystem.color3f(1F, 1F, 1F);

    float coverR = ((this.book.appearance.coverColor >> 16) & 0xff) / 255.F;
    float coverG = ((this.book.appearance.coverColor >> 8) & 0xff) / 255.F;
    float coverB = (this.book.appearance.coverColor & 0xff) / 255.F;

    TextureManager render = this.client.textureManager;

    if (this.page == -1) {
      render.bindTexture(TEX_BOOKFRONT);
      DiffuseLighting.disable();

      RenderSystem.color3f(coverR, coverG, coverB);
      drawTexture(matrixStack, this.width / 2 - PAGE_WIDTH_UNSCALED / 2, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);
      RenderSystem.color3f(1F, 1F, 1F);

      if (!this.book.appearance.title.isEmpty()) {
        drawTexture(matrixStack, this.width / 2 - PAGE_WIDTH_UNSCALED / 2, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        RenderSystem.pushMatrix();

        float scale = fontRenderer.getWidth(this.book.appearance.title) <= 67 ? 2.5F : 2F;

        RenderSystem.scalef(scale, scale, 1F);
        fontRenderer.drawWithShadow(matrixStack, this.book.appearance.title, (this.width / 2) / scale + 3 - fontRenderer.getWidth(this.book.appearance.title) / 2, (this.height / 2 - fontRenderer.fontHeight / 2) / scale - 4, 0xAE8000);
        RenderSystem.popMatrix();
      }

      if (!this.book.appearance.subtitle.isEmpty()) {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.5F, 1.5F, 1F);
        fontRenderer.drawWithShadow(matrixStack, this.book.appearance.subtitle, (this.width / 2) / 1.5F + 7 - fontRenderer.getWidth(this.book.appearance.subtitle) / 2, (this.height / 2 + 100 - fontRenderer.fontHeight * 2) / 1.5F, 0xAE8000);
        RenderSystem.popMatrix();
      }
    }
    else {
      render.bindTexture(TEX_BOOK);
      DiffuseLighting.disable();

      RenderSystem.color3f(coverR, coverG, coverB);
      drawTexture(matrixStack, this.width / 2 - PAGE_WIDTH_UNSCALED, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED * 2, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

      RenderSystem.color3f(1F, 1F, 1F);

      if (this.page != 0) {
        drawTexture(matrixStack, this.width / 2 - PAGE_WIDTH_UNSCALED, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        RenderSystem.pushMatrix();
        this.drawerTransform(false);

        RenderSystem.scalef(PAGE_SCALE, PAGE_SCALE, 1F);

        if (this.book.appearance.drawPageNumbers) {
          String pNum = (this.page - 1) * 2 + 2 + "";
          fontRenderer.draw(matrixStack, pNum, PAGE_WIDTH / 2 - fontRenderer.getWidth(pNum) / 2, PAGE_HEIGHT - 10, 0xFFAAAAAA);
        }

        int mX = this.getMouseX(false);
        int mY = this.getMouseY();

        // Not foreach to prevent conmodification crashes
        for (int i = 0; i < this.leftElements.size(); i++) {
          BookElement element = this.leftElements.get(i);

          RenderSystem.color4f(1F, 1F, 1F, 1F);
          element.draw(matrixStack, mX, mY, partialTicks, fontRenderer);
        }

        // Not foreach to prevent conmodification crashes
        for (int i = 0; i < this.leftElements.size(); i++) {
          BookElement element = this.leftElements.get(i);

          RenderSystem.color4f(1F, 1F, 1F, 1F);
          element.drawOverlay(matrixStack, mX, mY, partialTicks, fontRenderer);
        }

        RenderSystem.popMatrix();
      }

      // Rebind texture as the font renderer binds its own texture
      render.bindTexture(TEX_BOOK);
      // Set color back to white
      RenderSystem.color4f(1F, 1F, 1F, 1F);
      DiffuseLighting.disable();

      int fullPageCount = this.book.getFullPageCount(this.advancementCache);
      if (this.page < fullPageCount - 1 || this.book.getPageCount(this.advancementCache) % 2 != 0) {
        drawTexture(matrixStack, this.width / 2, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        RenderSystem.pushMatrix();
        this.drawerTransform(true);

        RenderSystem.scalef(PAGE_SCALE, PAGE_SCALE, 1F);

        if (this.book.appearance.drawPageNumbers) {
          String pNum = (this.page - 1) * 2 + 3 + "";
          fontRenderer.draw(matrixStack, pNum, PAGE_WIDTH / 2 - fontRenderer.getWidth(pNum) / 2, PAGE_HEIGHT - 10, 0xFFAAAAAA);
        }

        int mX = this.getMouseX(true);
        int mY = this.getMouseY();

        // Not foreach to prevent conmodification crashes
        for (int i = 0; i < this.rightElements.size(); i++) {
          BookElement element = this.rightElements.get(i);

          RenderSystem.color4f(1F, 1F, 1F, 1F);
          element.draw(matrixStack, mX, mY, partialTicks, fontRenderer);
        }

        // Not foreach to prevent conmodification crashes
        for (int i = 0; i < this.rightElements.size(); i++) {
          BookElement element = this.rightElements.get(i);

          RenderSystem.color4f(1F, 1F, 1F, 1F);
          element.drawOverlay(matrixStack, mX, mY, partialTicks, fontRenderer);
        }

        RenderSystem.popMatrix();
      }
    }

    super.render(matrixStack, mouseX, mouseY, partialTicks);

    RenderSystem.popMatrix();
  }

  @Override
  protected void init() {
    super.init();

    // The books are unreadable at Gui Scale set to small, so we'll double the scale, and of course half the size so that all our code still works as it should
    /*
    if(mc.gameSettings.guiScale == 1) {
      width /= 2F;
      height /= 2F;
    }*/

    this.buttons.clear();
    this.children.clear();

    this.previousArrow = this.addButton(new ArrowButton(-50, -50, ArrowButton.ArrowType.PREV, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.page--;

      if (this.page < -1) {
        this.page = -1;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.nextArrow = this.addButton(new ArrowButton(-50, -50, ArrowButton.ArrowType.NEXT, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.page++;

      int fullPageCount = this.book.getFullPageCount(this.advancementCache);

      if (this.page >= fullPageCount) {
        this.page = fullPageCount - 1;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.backArrow = this.addButton(new ArrowButton(this.width / 2 - ArrowButton.WIDTH / 2, this.height / 2 + ArrowButton.HEIGHT / 2 + PAGE_HEIGHT / 2, ArrowButton.ArrowType.LEFT, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      if (this.oldPage >= -1) {
        this.page = this.oldPage;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.indexArrow = this.addButton(new ArrowButton(this.width / 2 - PAGE_WIDTH_UNSCALED - ArrowButton.WIDTH / 2, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, ArrowButton.ArrowType.BACK_UP, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.openPage(this.book.findPageNumber("index.page1"));

      this.oldPage = -2;
      this.buildPages();
    }));

    this.buildPages();
  }

  @Override
  public void tick() {
    super.tick();

    this.previousArrow.visible = this.page != -1;
    this.nextArrow.visible = this.page + 1 < this.book.getFullPageCount(this.advancementCache);
    this.backArrow.visible = this.oldPage >= -1;

    if (this.page == -1) {
      this.nextArrow.x = this.width / 2 + 80;
      this.indexArrow.visible = false;
    }
    else {
      this.previousArrow.x = this.width / 2 - 184;
      this.nextArrow.x = this.width / 2 + 165;

      this.indexArrow.visible = this.book.findSection("index") != null && (this.page - 1) * 2 + 2 > this.book.findSection("index").getPageCount();
    }

    this.previousArrow.y = this.height / 2 + 75;
    this.nextArrow.y = this.height / 2 + 75;
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

    switch (keyCode) {
      case GLFW.GLFW_KEY_LEFT:
      case GLFW.GLFW_KEY_A:
        this.page--;
        if (this.page < -1) {
          this.page = -1;
        }

        this.oldPage = -2;
        this.buildPages();
        return true;
      case GLFW.GLFW_KEY_RIGHT:
      case GLFW.GLFW_KEY_D:
        this.page++;
        int fullPageCount = this.book.getFullPageCount(this.advancementCache);
        if (this.page >= fullPageCount) {
          this.page = fullPageCount - 1;
        }
        this.oldPage = -2;
        this.buildPages();
        return true;
      case GLFW.GLFW_KEY_F3:
        debug = !debug;
        return true;
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean mouseScrolled(double unKnown1, double unKnown2, double scrollDelta) {
    if (scrollDelta < 0.0D) {
      this.page++;
      int fullPageCount = this.book.getFullPageCount(this.advancementCache);
      if (this.page >= fullPageCount) {
        this.page = fullPageCount - 1;
      }
      this.oldPage = -2;
      this.buildPages();

      return true;
    }
    else if (scrollDelta > 0.0D) {
      this.page--;
      if (this.page < -1) {
        this.page = -1;
      }

      this.oldPage = -2;
      this.buildPages();

      return true;
    }

    return super.mouseScrolled(scrollDelta, unKnown1, unKnown2);
  }

  @Override
  public boolean mouseClicked(double originalMouseX, double originalMouseY, int mouseButton) {
    boolean right = false;

    double mouseX = this.getMouseX(false);
    double mouseY = this.getMouseY();

    if (mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = this.getMouseX(true);
      right = true;
    }

    // Not foreach to prevent conmodification crashes
    int oldPage = this.page;
    List<BookElement> elementList = ImmutableList.copyOf(right ? this.rightElements : this.leftElements);
    for (BookElement element : elementList) {
      element.mouseClicked(mouseX, mouseY, mouseButton);
      // if we changed page stop so we don't act on the new page
      if (this.page != oldPage) {
        return true;
      }
    }

    return super.mouseClicked(originalMouseX, originalMouseY, mouseButton);
  }

  @Override
  public boolean mouseReleased(double originalMouseX, double originalMouseY, int mouseButton) {
    boolean right = false;
    double mouseX = this.getMouseX(false);
    double mouseY = this.getMouseY();

    if (mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = this.getMouseX(true);
      right = true;
    }

    // Not foreach to prevent conmodification crashes
    for (int i = 0; right ? i < this.rightElements.size() : i < this.leftElements.size(); i++) {
      BookElement element = right ? this.rightElements.get(i) : this.leftElements.get(i);
      element.mouseReleased(mouseX, mouseY, mouseButton);
    }
    return super.mouseReleased(originalMouseX, originalMouseY, mouseButton);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLaslick, double unknown) {
    boolean right = false;
    mouseX = this.getMouseX(false);
    mouseY = this.getMouseY();

    if (mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = this.getMouseX(true);
      right = true;
    }

    // Not foreach to prevent conmodification crashes
    for (int i = 0; right ? i < this.rightElements.size() : i < this.leftElements.size(); i++) {
      BookElement element = right ? this.rightElements.get(i) : this.leftElements.get(i);
      element.mouseClickMove(mouseX, mouseY, clickedMouseButton);
    }

    return true;
  }

  @Override
  public void removed() {
    if (this.client.player == null) {
      return;
    }

    PageData page = this.page == 0 ? this.book.findPage(0, this.advancementCache) : this.book.findPage((this.page - 1) * 2 + 1, this.advancementCache);

    if (page == null) {
      page = this.book.findPage((this.page - 1) * 2 + 2, this.advancementCache);
    }

    if (this.page == -1) {
      BookLoader.updateSavedPage(this.client.player, this.item, "");
    }
    else if (page != null && page.parent != null) {
      BookLoader.updateSavedPage(this.client.player, this.item, page.parent.name + "." + page.name);
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  public void drawerTransform(boolean rightSide) {
    if (rightSide) {
      RenderSystem.translatef(this.width / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    }
    else {
      RenderSystem.translatef(this.width / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    }
  }

  // offset to the left edge of the left/right side
  protected float leftOffset(boolean rightSide) {
    if (rightSide) {
      // from center: go padding + margin to the right
      return this.width / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN;
    }
    else {
      // from center: go page width left, then right with padding and margin
      return this.width / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN;
    }
  }

  protected float topOffset() {
    return this.height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN;
  }

  protected int getMouseX(boolean rightSide) {
    return (int) ((MinecraftClient.getInstance().mouse.getX() * this.width / this.client.getWindow().getFramebufferWidth() - this.leftOffset(rightSide)) / PAGE_SCALE);
  }

  protected int getMouseY() {
    return (int) ((MinecraftClient.getInstance().mouse.getY() * this.height / this.client.getWindow().getFramebufferHeight() - 1 - this.topOffset()) / PAGE_SCALE);
  }

  public int openPage(int page) {
    return this.openPage(page, false);
  }

  public int openPage(int page, boolean returner) {
    if (page < 0) {
      return -1;
    }

    int bookPage;
    if (page == 1) {
      bookPage = 0;
    }
    else if (page % 2 == 0) {
      bookPage = (page - 1) / 2 + 1;
    }
    else {
      bookPage = (page - 2) / 2 + 1;
    }

    if (bookPage >= -1 && bookPage < this.book.getFullPageCount(this.advancementCache)) {
      if (returner) {
        this.oldPage = this.page;
      }

      this._setPage(bookPage);
    }

    return page % 2 == 0 ? 0 : 1;
  }

  public void _setPage(int page) {
    this.page = page;
    this.buildPages();
  }

  public int getPage(int side) {
    if (this.page == 0 && side == 0) {
      return -1;
    }
    else if (this.page == 0 && side == 1) {
      return 0;
    }
    else if (side == 0) {
      return (this.page - 1) * 2 + 1;
    }
    else if (side == 1) {
      return (this.page - 2) * 2 + 2;
    }
    else {
      return -1;
    }
  }

  public int getPage_() {
    return this.page;
  }

  public ArrayList<BookElement> getElements(int side) {
    return side == 0 ? this.leftElements : side == 1 ? this.rightElements : null;
  }

  public void openCover() {
    this._setPage(-1);

    this.leftElements.clear();
    this.rightElements.clear();
    this.buildPages();
  }

  public void itemClicked(ItemStack item) {
    StringActionProcessor.process(this.book.getItemAction(ItemStackData.getItemStackData(item, true)), this);
  }

  private void buildPages() {
    this.leftElements.clear();
    this.rightElements.clear();

    if (this.page == -1) {
      return;
    }

    if (this.page == 0) {
      PageData page = this.book.findPage(0, this.advancementCache);

      if (page != null) {
        page.content.build(this.book, this.rightElements, false);
      }
    }
    else {
      PageData leftPage = this.book.findPage((this.page - 1) * 2 + 1, this.advancementCache);
      PageData rightPage = this.book.findPage((this.page - 1) * 2 + 2, this.advancementCache);

      if (leftPage != null) {
        leftPage.content.build(this.book, this.leftElements, false);
      }
      if (rightPage != null) {
        rightPage.content.build(this.book, this.rightElements, true);
      }
    }

    for (BookElement element : this.leftElements) {
      element.parent = this;
    }
    for (BookElement element : this.rightElements) {
      element.parent = this;
    }
  }

  public class AdvancementCache implements ClientAdvancementManager.Listener {

    private HashMap<Advancement, AdvancementProgress> progress = new HashMap<>();
    private HashMap<Identifier, Advancement> nameCache = new HashMap<>();

    @Nullable
    public AdvancementProgress getProgress(String id) {
      return this.getProgress(this.getAdvancement(id));
    }

    @Nullable
    public AdvancementProgress getProgress(Advancement advancement) {
      return this.progress.get(advancement);
    }

    public Advancement getAdvancement(String id) {
      return this.nameCache.get(new Identifier(id));
    }

    @Override
    public void setProgress(Advancement advancement, AdvancementProgress advancementProgress) {
      this.progress.put(advancement, advancementProgress);
    }

    @Override
    public void selectTab(@Nullable Advancement advancement) {
      // noop
    }

    @Override
    public void onRootAdded(Advancement advancement) {
      this.nameCache.put(advancement.getId(), advancement);
    }

    @Override
    public void onRootRemoved(Advancement advancement) {
      this.progress.remove(advancement);
      this.nameCache.remove(advancement.getId());
    }

    @Override
    public void onDependentAdded(Advancement advancement) {
      this.nameCache.put(advancement.getId(), advancement);
    }

    @Override
    public void onDependentRemoved(Advancement advancement) {
      this.progress.remove(advancement);
      this.nameCache.remove(advancement.getId());
    }

    @Override
    public void onClear() {
      this.progress.clear();
      this.nameCache.clear();
    }
  }
}

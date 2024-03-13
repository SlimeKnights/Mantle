package slimeknights.mantle.client.screen.book;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.screen.book.element.BookElement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class BookScreen extends Screen {

  public static boolean debug = false;

  public static final int TEX_SIZE = 512;

  public static final int PAGE_MARGIN = 8;

  public static final int PAGE_PADDING_TOP = 4;
  public static final int PAGE_PADDING_BOT = 4;
  public static final int PAGE_PADDING_LEFT = 8;
  public static final int PAGE_PADDING_RIGHT = 0;

  public static final float PAGE_SCALE = 1f;
  public static final int PAGE_WIDTH_UNSCALED = 206;
  public static final int PAGE_HEIGHT_UNSCALED = 200;

  // For best results, make sure both PAGE_WIDTH_UNSCALED - (PAGE_PADDING + PAGE_MARGIN) * 2 and PAGE_HEIGHT_UNSCALED - (PAGE_PADDING + PAGE_MARGIN) * 2 divide evenly into PAGE_SCALE (without remainder)
  public static final int PAGE_WIDTH = (int) ((PAGE_WIDTH_UNSCALED - (PAGE_PADDING_LEFT + PAGE_PADDING_RIGHT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);
  public static final int PAGE_HEIGHT = (int) ((PAGE_HEIGHT_UNSCALED - (PAGE_PADDING_TOP + PAGE_PADDING_BOT + PAGE_MARGIN + PAGE_MARGIN)) / PAGE_SCALE);

  private ArrowButton previousArrow, nextArrow, backArrow, indexArrow;

  public final BookData book;
  @Nullable
  private final Consumer<String> pageUpdater;
  @Nullable
  private final Consumer<?> bookPickup;

  private int page = -1;
  private int oldPage = -2;
  private final ArrayList<BookElement> leftElements = new ArrayList<>();
  private final ArrayList<BookElement> rightElements = new ArrayList<>();

  public AdvancementCache advancementCache;

  private double[] lastClick;
  private double[] lastDrag;

  // TODO: maybe make this a list with ability to add custom layers
  private static final ILayerRenderFunction[] LAYERS = {
    // Main layer
    BookElement::draw,
    // Overlay layer
    BookElement::drawOverlay
  };

  public BookScreen(Component title, BookData book, String page, @Nullable Consumer<String> pageUpdater, @Nullable Consumer<?> bookPickup) {
    super(title);
    this.book = book;
    this.pageUpdater = pageUpdater;
    this.bookPickup = bookPickup;

    this.minecraft = Minecraft.getInstance();
    this.font = this.minecraft.font;

    this.advancementCache = new AdvancementCache();
    if (this.minecraft.player != null) {
      this.minecraft.player.connection.getAdvancements().setListener(this.advancementCache);
    }
    this.openPage(book.findPageNumber(page, this.advancementCache));
  }

  public Font getFontRenderer() {
    Font fontRenderer = this.book.fontRenderer;
    if (fontRenderer == null) {
      fontRenderer = Objects.requireNonNull(this.minecraft).font;
    }

    return fontRenderer;
  }

  private Vector3f splitRGB(int color) {
    float r = FastColor.ARGB32.red(color) / 255.F;
    float g = FastColor.ARGB32.green(color)  / 255.F;
    float b = FastColor.ARGB32.blue(color)  / 255.F;

    return new Vector3f(r, g, b);
  }

  private Vector4f splitRGBA(int color) {
    float r = FastColor.ARGB32.red(color) / 255.F;
    float g = FastColor.ARGB32.green(color)  / 255.F;
    float b = FastColor.ARGB32.blue(color)  / 255.F;
    float a = FastColor.ARGB32.alpha(color)  / 255.F;

    return new Vector4f(r, g, b, a);
  }

  @Override
  public void render(PoseStack matrixStack, int mouseX ,int mouseY, float partialTicks) {
    if(this.minecraft == null) {
      return;
    }

    Font fontRenderer = getFontRenderer();

    if (debug) {
      fill(matrixStack, 0, 0, fontRenderer.width("DEBUG") + 4, fontRenderer.lineHeight + 4, 0x55000000);
      fontRenderer.draw(matrixStack, "DEBUG", 2, 2, 0xFFFFFFFF);
    }

    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    // RenderSystem.enableAlphaTest(); TODO: still needed?
    RenderSystem.enableBlend();

    Vector3f coverColor = splitRGB(this.book.appearance.coverColor);

    if(this.page == -1) {
      this.renderCover(matrixStack, coverColor);
    } else {
      // Jank way to copy last matrix in matrix stack, as no proper way is provided
      PoseStack leftMatrix = new PoseStack();
      leftMatrix.last().pose().multiply(matrixStack.last().pose());
      leftMatrix.last().normal().mul(matrixStack.last().normal());

      PoseStack rightMatrix = new PoseStack();
      rightMatrix.last().pose().multiply(matrixStack.last().pose());
      rightMatrix.last().normal().mul(matrixStack.last().normal());

      drawerTransform(leftMatrix, false);
      drawerTransform(rightMatrix, true);

      leftMatrix.scale(PAGE_SCALE, PAGE_SCALE, 1F);
      rightMatrix.scale(PAGE_SCALE, PAGE_SCALE, 1F);

      boolean renderLeft = shouldRenderPage(this.page, false);
      boolean renderRight = shouldRenderPage(this.page, true);

      renderUnderLayer(matrixStack, coverColor);

      if(renderLeft) {
        renderPageBackground(matrixStack, false);
      }

      if(renderRight) {
        renderPageBackground(matrixStack, true);
      }

      int leftMX = this.getMouseX(false);
      int rightMX = this.getMouseX(true);
      int mY = this.getMouseY();

      for (ILayerRenderFunction layer : LAYERS) {
        if(renderLeft) {
          renderPageLayer(leftMatrix, leftMX, mY, partialTicks, leftElements, layer);
        }

        if(renderRight) {
          renderPageLayer(rightMatrix, rightMX, mY, partialTicks, rightElements, layer);
        }
      }
    }

    super.render(matrixStack, mouseX, mouseY, partialTicks);
  }

  private boolean shouldRenderPage(int pageNum, boolean rightSide) {
    if(!rightSide) {
      return pageNum != 0;
    }

    int fullPageCount = this.book.getFullPageCount(this.advancementCache);
    return this.page < fullPageCount - 1 || this.book.getPageCount(this.advancementCache) % 2 != 0;
  }

  private void renderCover(PoseStack matrixStack, Vector3f coverColor) {
    Font fontRenderer = getFontRenderer();

    RenderSystem.setShaderTexture(0, book.appearance.getCoverTexture());

    int centerX = this.width / 2 - PAGE_WIDTH_UNSCALED / 2;
    int centerY = this.height / 2 - PAGE_HEIGHT_UNSCALED / 2;

    RenderSystem.setShaderColor(coverColor.x(), coverColor.y(), coverColor.z(), 1.0f);
    blit(matrixStack, centerX, centerY, 0, 0, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);
    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

    if (!this.book.appearance.title.isEmpty()) {
      blit(matrixStack, centerX, centerY, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

      matrixStack.pushPose();

      int width = fontRenderer.width(this.book.appearance.title);
      float scale = Mth.clamp((float)PAGE_WIDTH / width, 0F, 2.5F);

      matrixStack.scale(scale, scale, 1F);

      fontRenderer.drawShadow(matrixStack, this.book.appearance.title, (this.width / 2F) / scale + 3 - width / 2F, (this.height / 2F - fontRenderer.lineHeight / 2F) / scale - 4, this.book.appearance.getCoverTextColor());
      matrixStack.popPose();
    }

    if (!this.book.appearance.subtitle.isEmpty()) {
      matrixStack.pushPose();

      int width = fontRenderer.width(this.book.appearance.subtitle);
      float scale = Mth.clamp((float)PAGE_WIDTH / width, 0F, 1.5F);

      matrixStack.scale(scale, scale, 1F);
      fontRenderer.drawShadow(matrixStack, this.book.appearance.subtitle, (this.width / 2F) / scale + 7 - width / 2F, (this.height / 2F + 100 - fontRenderer.lineHeight * 2) / scale, this.book.appearance.getCoverTextColor());
      matrixStack.popPose();
    }
  }

  private void renderUnderLayer(PoseStack matrixStack, Vector3f coverColor) {
    RenderSystem.setShaderTexture(0, this.book.appearance.getBookTexture());
    RenderSystem.setShaderColor(coverColor.x(), coverColor.y(), coverColor.z(), 1f);

    blit(matrixStack, this.width / 2 - PAGE_WIDTH_UNSCALED, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED * 2, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);
  }

  private void renderPageBackground(PoseStack matrixStack, boolean rightSide) {
    Vector3f pageTint = splitRGB(this.book.appearance.getPageTint());
    RenderSystem.setShaderColor(pageTint.x(), pageTint.y(), pageTint.z(), 1f);

    if(!rightSide) {
      blit(matrixStack, this.width / 2 - PAGE_WIDTH_UNSCALED, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);
    } else {
      blit(matrixStack, this.width / 2, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);
    }
  }

  private void renderPageLayer(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, List<BookElement> elements, ILayerRenderFunction layerFunc) {
    RenderSystem.setShaderTexture(0, book.appearance.getCoverTexture());

    Font font = getFontRenderer();

    for(BookElement element : elements) {
      RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
      layerFunc.draw(element, matrixStack, mouseX, mouseY, partialTicks, font);
    }
  }

  @Override
  protected void init() {
    super.init();

    clearWidgets();

    this.previousArrow = this.addRenderableWidget(new ArrowButton(book, 50, -50, ArrowButton.ArrowType.PREV, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.page--;

      if (this.page < -1) {
        this.page = -1;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.nextArrow = this.addRenderableWidget(new ArrowButton(book, -50, -50, ArrowButton.ArrowType.NEXT, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.page++;

      int fullPageCount = this.book.getFullPageCount(this.advancementCache);

      if (this.page >= fullPageCount) {
        this.page = fullPageCount - 1;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.backArrow = this.addRenderableWidget(new ArrowButton(book, this.width / 2 - ArrowButton.WIDTH / 2, this.height / 2 + ArrowButton.HEIGHT / 2 + PAGE_HEIGHT / 2, ArrowButton.ArrowType.LEFT, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      if (this.oldPage >= -1) {
        this.page = this.oldPage;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.indexArrow = this.addRenderableWidget(new ArrowButton(book, this.width / 2 - PAGE_WIDTH_UNSCALED - ArrowButton.WIDTH / 2, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2, ArrowButton.ArrowType.BACK_UP, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.openPage(this.book.findPageNumber("index.page1", this.advancementCache));

      this.oldPage = -2;
      this.buildPages();
    }));

    if(this.bookPickup != null) {
      int margin = 10;
      if(this.height / 2 + PAGE_HEIGHT_UNSCALED / 2 + margin + 20 >= this.height) {
        margin = 0;
      }

      this.addRenderableWidget(new Button(this.width / 2 - 196 / 2, this.height / 2 + PAGE_HEIGHT_UNSCALED / 2 + margin, 196, 20, Component.translatable("lectern.take_book"), (p_212998_1_) -> {
        this.onClose();
        this.bookPickup.accept(null);
      }));
    }

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
    } else {
      this.previousArrow.x = this.width / 2 - 184;
      this.nextArrow.x = this.width / 2 + 165;

      SectionData index = this.book.findSection("index", this.advancementCache);
      this.indexArrow.visible = index != null && (this.page - 1) * 2 + 2 > index.getPageCount();
    }

    this.previousArrow.y = this.height / 2 + 75;
    this.nextArrow.y = this.height / 2 + 75;
  }

  /** Goes to the previous page */
  private void previousPage() {
    this.page--;
    if (this.page < -1) {
      this.page = -1;
    }
    this.oldPage = -2;
    this.buildPages();
  }

  /** Goes to the next page */
  private void nextPage() {
    this.page++;
    int fullPageCount = this.book.getFullPageCount(this.advancementCache);
    if (this.page >= fullPageCount) {
      this.page = fullPageCount - 1;
    }
    this.oldPage = -2;
    this.buildPages();
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    super.keyPressed(keyCode, scanCode, modifiers);

    switch (keyCode) {
      case GLFW.GLFW_KEY_LEFT, GLFW.GLFW_KEY_A -> {
        previousPage();
        return true;
      }
      case GLFW.GLFW_KEY_RIGHT, GLFW.GLFW_KEY_D -> {
        nextPage();
        return true;
      }
      case GLFW.GLFW_KEY_F3 -> {
        debug = !debug;
        return true;
      }
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean mouseScrolled(double unKnown1, double unKnown2, double scrollDelta) {
    if (scrollDelta < 0.0D) {
      nextPage();
      return true;
    } else if (scrollDelta > 0.0D) {
      previousPage();
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

    lastClick = new double[]{mouseX, mouseY};

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

    lastClick = null;
    lastDrag = null;

    return super.mouseReleased(originalMouseX, originalMouseY, mouseButton);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    boolean right = false;
    mouseX = this.getMouseX(false);
    mouseY = this.getMouseY();

    if (mouseX > PAGE_WIDTH + (PAGE_MARGIN + PAGE_PADDING_LEFT) / PAGE_SCALE) {
      mouseX = this.getMouseX(true);
      right = true;
    }

    if (lastClick != null) {
      if (lastDrag == null)
        lastDrag = new double[]{mouseX, mouseY};

      // Not foreach to prevent conmodification crashes
      for (int i = 0; right ? i < this.rightElements.size() : i < this.leftElements.size(); i++) {
        BookElement element = right ? this.rightElements.get(i) : this.leftElements.get(i);
        element.mouseDragged(lastClick[0], lastClick[1], mouseX, mouseY, lastDrag[0], lastDrag[1], button);
      }

      lastDrag = new double[]{mouseX, mouseY};
    }


    return true;
  }

  @Override
  public void removed() {
    if (this.minecraft == null || this.minecraft.player == null) {
      return;
    }
    // find what page to update
    if (pageUpdater != null) {
      String pageStr = "";
      if (this.page >= 0) {
        PageData page = this.page == 0 ? this.book.findPage(0, this.advancementCache) : this.book.findPage((this.page - 1) * 2 + 1, this.advancementCache);
        if (page == null) {
          page = this.book.findPage((this.page - 1) * 2 + 2, this.advancementCache);
        }
        if (page != null && page.parent != null) {
          pageStr = page.parent.name + "." + page.name;
        }
      }
      pageUpdater.accept(pageStr);
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  public void drawerTransform(PoseStack matrixStack, boolean rightSide) {
    if (rightSide) {
      matrixStack.translate(this.width / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    } else {
      matrixStack.translate(this.width / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN, this.height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    }
  }

  // offset to the left edge of the left/right side
  protected float leftOffset(boolean rightSide) {
    if (rightSide) {
      // from center: go padding + margin to the right
      return this.width / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN;
    } else {
      // from center: go page width left, then right with padding and margin
      return this.width / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN;
    }
  }

  protected float topOffset() {
    return this.height / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN;
  }

  protected int getMouseX(boolean rightSide) {
    assert this.minecraft != null;
    return (int) ((Minecraft.getInstance().mouseHandler.xpos() * this.width / this.minecraft.getWindow().getScreenWidth() - this.leftOffset(rightSide)) / PAGE_SCALE);
  }

  protected int getMouseY() {
    assert this.minecraft != null;
    return (int) ((Minecraft.getInstance().mouseHandler.ypos() * this.height / this.minecraft.getWindow().getScreenHeight() - 1 - this.topOffset()) / PAGE_SCALE);
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
    } else if (page % 2 == 0) {
      bookPage = (page - 1) / 2 + 1;
    } else {
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
    } else if (this.page == 0 && side == 1) {
      return 0;
    } else if (side == 0) {
      return (this.page - 1) * 2 + 1;
    } else if (side == 1) {
      return (this.page - 2) * 2 + 2;
    } else {
      return -1;
    }
  }

  public int getPage_() {
    return this.page;
  }

  public List<BookElement> getElements(int side) {
    return side == 0 ? this.leftElements : side == 1 ? this.rightElements : Collections.emptyList();
  }

  public void openCover() {
    this._setPage(-1);

    this.leftElements.clear();
    this.rightElements.clear();
    this.buildPages();
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
    } else {
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

  public static class AdvancementCache implements ClientAdvancements.Listener {

    private final HashMap<Advancement, AdvancementProgress> progress = new HashMap<>();
    private final HashMap<ResourceLocation, Advancement> nameCache = new HashMap<>();

    @Nullable
    public AdvancementProgress getProgress(String id) {
      return this.getProgress(this.getAdvancement(id));
    }

    @Nullable
    public AdvancementProgress getProgress(Advancement advancement) {
      return this.progress.get(advancement);
    }

    public Advancement getAdvancement(String id) {
      return this.nameCache.get(new ResourceLocation(id));
    }

    @Override
    public void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress advancementProgress) {
      this.progress.put(advancement, advancementProgress);
    }

    @Override
    public void onSelectedTabChanged(@Nullable Advancement advancement) {
      // noop
    }

    @Override
    public void onAddAdvancementRoot(Advancement advancement) {
      this.nameCache.put(advancement.getId(), advancement);
    }

    @Override
    public void onRemoveAdvancementRoot(Advancement advancement) {
      this.progress.remove(advancement);
      this.nameCache.remove(advancement.getId());
    }

    @Override
    public void onAddAdvancementTask(Advancement advancement) {
      this.nameCache.put(advancement.getId(), advancement);
    }

    @Override
    public void onRemoveAdvancementTask(Advancement advancement) {
      this.progress.remove(advancement);
      this.nameCache.remove(advancement.getId());
    }

    @Override
    public void onAdvancementsCleared() {
      this.progress.clear();
      this.nameCache.clear();
    }
  }
}

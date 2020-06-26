package slimeknights.mantle.client.screen.book;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ClientAdvancementManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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

@OnlyIn(Dist.CLIENT)
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

  public BookScreen(ITextComponent title, BookData book, @Nullable ItemStack item) {
    super(title);
    this.book = book;
    this.item = item;

    this.field_230706_i_ = Minecraft.getInstance();
    this.field_230712_o_ = this.field_230706_i_.fontRenderer;

    initWidthsAndHeights();

    this.advancementCache = new AdvancementCache();
    this.field_230706_i_.player.connection.getAdvancementManager().setListener(this.advancementCache);

    this.openPage(book.findPageNumber(BookHelper.getSavedPage(item), this.advancementCache));
  }

  @Override
  @SuppressWarnings("ForLoopReplaceableByForEach")
  public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    initWidthsAndHeights();
    FontRenderer fontRenderer = this.book.fontRenderer;
    if (fontRenderer == null) {
      fontRenderer = this.field_230706_i_.fontRenderer;
    }

    if (debug) {
      func_238467_a_(matrixStack, 0, 0, fontRenderer.getStringWidth("DEBUG") + 4, fontRenderer.FONT_HEIGHT + 4, 0x55000000);
      fontRenderer.func_238421_b_(matrixStack, "DEBUG", 2, 2, 0xFFFFFFFF);
    }

    RenderSystem.enableAlphaTest();
    RenderSystem.enableBlend();

    // The books are unreadable at Gui Scale set to small, so we'll double the scale
    RenderSystem.pushMatrix();
    RenderSystem.color3f(1F, 1F, 1F);

    float coverR = ((this.book.appearance.coverColor >> 16) & 0xff) / 255.F;
    float coverG = ((this.book.appearance.coverColor >> 8) & 0xff) / 255.F;
    float coverB = (this.book.appearance.coverColor & 0xff) / 255.F;

    TextureManager render = this.field_230706_i_.textureManager;

    if (this.page == -1) {
      render.bindTexture(TEX_BOOKFRONT);
      RenderHelper.disableStandardItemLighting();

      RenderSystem.color3f(coverR, coverG, coverB);
      func_238463_a_(matrixStack, this.field_230708_k_ / 2 - PAGE_WIDTH_UNSCALED / 2, this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);
      RenderSystem.color3f(1F, 1F, 1F);

      if (!this.book.appearance.title.isEmpty()) {
        func_238463_a_(matrixStack, this.field_230708_k_ / 2 - PAGE_WIDTH_UNSCALED / 2, this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        RenderSystem.pushMatrix();

        float scale = fontRenderer.getStringWidth(this.book.appearance.title) <= 67 ? 2.5F : 2F;

        RenderSystem.scalef(scale, scale, 1F);
        fontRenderer.func_238405_a_(matrixStack, this.book.appearance.title, (this.field_230708_k_ / 2) / scale + 3 - fontRenderer.getStringWidth(this.book.appearance.title) / 2, (this.field_230709_l_ / 2 - fontRenderer.FONT_HEIGHT / 2) / scale - 4, 0xAE8000);
        RenderSystem.popMatrix();
      }

      if (!this.book.appearance.subtitle.isEmpty()) {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.5F, 1.5F, 1F);
        fontRenderer.func_238405_a_(matrixStack, this.book.appearance.subtitle, (this.field_230708_k_ / 2) / 1.5F + 7 - fontRenderer.getStringWidth(this.book.appearance.subtitle) / 2, (this.field_230709_l_ / 2 + 100 - fontRenderer.FONT_HEIGHT * 2) / 1.5F, 0xAE8000);
        RenderSystem.popMatrix();
      }
    }
    else {
      render.bindTexture(TEX_BOOK);
      RenderHelper.disableStandardItemLighting();

      RenderSystem.color3f(coverR, coverG, coverB);
      func_238463_a_(matrixStack, this.field_230708_k_ / 2 - PAGE_WIDTH_UNSCALED, this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, 0, PAGE_WIDTH_UNSCALED * 2, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

      RenderSystem.color3f(1F, 1F, 1F);

      if (this.page != 0) {
        func_238463_a_(matrixStack, this.field_230708_k_ / 2 - PAGE_WIDTH_UNSCALED, this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2, 0, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        RenderSystem.pushMatrix();
        this.drawerTransform(false);

        RenderSystem.scalef(PAGE_SCALE, PAGE_SCALE, 1F);

        if (this.book.appearance.drawPageNumbers) {
          String pNum = (this.page - 1) * 2 + 2 + "";
          fontRenderer.func_238421_b_(matrixStack, pNum, PAGE_WIDTH / 2 - fontRenderer.getStringWidth(pNum) / 2, PAGE_HEIGHT - 10, 0xFFAAAAAA);
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
      RenderHelper.disableStandardItemLighting();

      int fullPageCount = this.book.getFullPageCount(this.advancementCache);
      if (this.page < fullPageCount - 1 || this.book.getPageCount(this.advancementCache) % 2 != 0) {
        func_238463_a_(matrixStack, this.field_230708_k_ / 2, this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, PAGE_WIDTH_UNSCALED, PAGE_HEIGHT_UNSCALED, TEX_SIZE, TEX_SIZE);

        RenderSystem.pushMatrix();
        this.drawerTransform(true);

        RenderSystem.scalef(PAGE_SCALE, PAGE_SCALE, 1F);

        if (this.book.appearance.drawPageNumbers) {
          String pNum = (this.page - 1) * 2 + 3 + "";
          fontRenderer.func_238421_b_(matrixStack, pNum, PAGE_WIDTH / 2 - fontRenderer.getStringWidth(pNum) / 2, PAGE_HEIGHT - 10, 0xFFAAAAAA);
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

    super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);

    RenderSystem.popMatrix();
  }

  @Override
  protected void func_231160_c_() {
    super.func_231160_c_();

    // The books are unreadable at Gui Scale set to small, so we'll double the scale, and of course half the size so that all our code still works as it should
    /*
    if(mc.gameSettings.guiScale == 1) {
      width /= 2F;
      height /= 2F;
    }*/

    this.field_230710_m_.clear();
    this.field_230705_e_.clear();

    this.previousArrow = this.func_230480_a_(new ArrowButton(-50, -50, ArrowButton.ArrowType.PREV, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.page--;

      if (this.page < -1) {
        this.page = -1;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.nextArrow = this.func_230480_a_(new ArrowButton(-50, -50, ArrowButton.ArrowType.NEXT, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.page++;

      int fullPageCount = this.book.getFullPageCount(this.advancementCache);

      if (this.page >= fullPageCount) {
        this.page = fullPageCount - 1;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.backArrow = this.func_230480_a_(new ArrowButton(this.field_230708_k_ / 2 - ArrowButton.WIDTH / 2, this.field_230709_l_ / 2 + ArrowButton.HEIGHT / 2 + PAGE_HEIGHT / 2, ArrowButton.ArrowType.LEFT, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      if (this.oldPage >= -1) {
        this.page = this.oldPage;
      }

      this.oldPage = -2;
      this.buildPages();
    }));

    this.indexArrow = this.func_230480_a_(new ArrowButton(this.field_230708_k_ / 2 - PAGE_WIDTH_UNSCALED - ArrowButton.WIDTH / 2, this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2, ArrowButton.ArrowType.BACK_UP, this.book.appearance.arrowColor, this.book.appearance.arrowColorHover, (p_212998_1_) -> {
      this.openPage(this.book.findPageNumber("index.page1"));

      this.oldPage = -2;
      this.buildPages();
    }));

    this.buildPages();
  }

  @Override
  public void func_231023_e_() {
    super.func_231023_e_();

    this.previousArrow.field_230694_p_ = this.page != -1;
    this.nextArrow.field_230694_p_ = this.page + 1 < this.book.getFullPageCount(this.advancementCache);
    this.backArrow.field_230694_p_ = this.oldPage >= -1;

    if (this.page == -1) {
      this.nextArrow.field_230690_l_ = this.field_230708_k_ / 2 + 80;
      this.indexArrow.field_230694_p_ = false;
    }
    else {
      this.previousArrow.field_230690_l_ = this.field_230708_k_ / 2 - 184;
      this.nextArrow.field_230690_l_ = this.field_230708_k_ / 2 + 165;

      this.indexArrow.field_230694_p_ = this.book.findSection("index") != null && (this.page - 1) * 2 + 2 > this.book.findSection("index").getPageCount();
    }

    this.previousArrow.field_230691_m_ = this.field_230709_l_ / 2 + 75;
    this.nextArrow.field_230691_m_ = this.field_230709_l_ / 2 + 75;
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
  public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
    super.func_231046_a_(keyCode, scanCode, modifiers);

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

    return super.func_231046_a_(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean func_231043_a_(double unKnown1, double unKnown2, double scrollDelta) {

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

    return super.func_231043_a_(scrollDelta, unKnown1, unKnown2);
  }

  @Override
  public boolean func_231044_a_(double originalMouseX, double originalMouseY, int mouseButton) {
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

    return super.func_231044_a_(originalMouseX, originalMouseY, mouseButton);
  }

  @Override
  public boolean func_231048_c_(double originalMouseX, double originalMouseY, int mouseButton) {
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
    return super.func_231048_c_(originalMouseX, originalMouseY, mouseButton);
  }

  @Override
  public boolean func_231045_a_(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLaslick, double unknown) {
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
  public void func_231164_f_() {
    if (this.field_230706_i_.player == null) {
      return;
    }

    PageData page = this.page == 0 ? this.book.findPage(0, this.advancementCache) : this.book.findPage((this.page - 1) * 2 + 1, this.advancementCache);

    if (page == null) {
      page = this.book.findPage((this.page - 1) * 2 + 2, this.advancementCache);
    }

    if (this.page == -1) {
      BookLoader.updateSavedPage(this.field_230706_i_.player, this.item, "");
    }
    else if (page != null && page.parent != null) {
      BookLoader.updateSavedPage(this.field_230706_i_.player, this.item, page.parent.name + "." + page.name);
    }
  }

  @Override
  public boolean func_231177_au__() {
    return false;
  }

  public void drawerTransform(boolean rightSide) {
    if (rightSide) {
      RenderSystem.translatef(this.field_230708_k_ / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN, this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    }
    else {
      RenderSystem.translatef(this.field_230708_k_ / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN, this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN, 0);
    }
  }

  // offset to the left edge of the left/right side
  protected float leftOffset(boolean rightSide) {
    if (rightSide) {
      // from center: go padding + margin to the right
      return this.field_230708_k_ / 2 + PAGE_PADDING_RIGHT + PAGE_MARGIN;
    }
    else {
      // from center: go page width left, then right with padding and margin
      return this.field_230708_k_ / 2 - PAGE_WIDTH_UNSCALED + PAGE_PADDING_LEFT + PAGE_MARGIN;
    }
  }

  protected float topOffset() {
    return this.field_230709_l_ / 2 - PAGE_HEIGHT_UNSCALED / 2 + PAGE_PADDING_TOP + PAGE_MARGIN;
  }

  protected int getMouseX(boolean rightSide) {
    return (int) ((Minecraft.getInstance().mouseHelper.getMouseX() * this.field_230708_k_ / this.field_230706_i_.getMainWindow().getFramebufferWidth() - this.leftOffset(rightSide)) / PAGE_SCALE);
  }

  protected int getMouseY() {
    return (int) ((Minecraft.getInstance().mouseHelper.getMouseY() * this.field_230709_l_ / this.field_230706_i_.getMainWindow().getFramebufferHeight() - 1 - this.topOffset()) / PAGE_SCALE);
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

  public class AdvancementCache implements ClientAdvancementManager.IListener {

    private HashMap<Advancement, AdvancementProgress> progress = new HashMap<>();
    private HashMap<ResourceLocation, Advancement> nameCache = new HashMap<>();

    public AdvancementProgress getProgress(String id) {
      return this.getProgress(this.getAdvancement(id));
    }

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
    public void setSelectedTab(@Nullable Advancement advancement) {
      // noop
    }

    @Override
    public void rootAdvancementAdded(Advancement advancement) {
      this.nameCache.put(advancement.getId(), advancement);
    }

    @Override
    public void rootAdvancementRemoved(Advancement advancement) {
      this.progress.remove(advancement);
      this.nameCache.remove(advancement.getId());
    }

    @Override
    public void nonRootAdvancementAdded(Advancement advancement) {
      this.nameCache.put(advancement.getId(), advancement);
    }

    @Override
    public void nonRootAdvancementRemoved(Advancement advancement) {
      this.progress.remove(advancement);
      this.nameCache.remove(advancement.getId());
    }

    @Override
    public void advancementsCleared() {
      this.progress.clear();
      this.nameCache.clear();
    }
  }
}

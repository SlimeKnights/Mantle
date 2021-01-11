package slimeknights.mantle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import slimeknights.mantle.inventory.MultiModuleContainer;
import slimeknights.mantle.inventory.WrapperSlot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiModuleScreen<CONTAINER extends MultiModuleContainer<?>> extends ContainerScreen<CONTAINER> {

  protected List<ModuleScreen<?,?>> modules = Lists.newArrayList();

  public int cornerX;
  public int cornerY;
  public int realWidth;
  public int realHeight;

  public MultiModuleScreen(CONTAINER container, PlayerInventory playerInventory, ITextComponent title) {
    super(container, playerInventory, title);

    this.realWidth = -1;
    this.realHeight = -1;
    this.passEvents = true;
  }

  protected void addModule(ModuleScreen<?,?> module) {
    this.modules.add(module);
  }

  public List<Rectangle2d> getModuleAreas() {
    List<Rectangle2d> areas = new ArrayList<>(this.modules.size());
    for (ModuleScreen<?,?> module : this.modules) {
      areas.add(module.getArea());
    }
    return areas;
  }

  @Override
  public void init() {
    if (this.realWidth > -1) {
      // has to be reset before calling initGui so the position is getting retained
      this.xSize = this.realWidth;
      this.ySize = this.realHeight;
    }

    super.init();

    this.cornerX = this.guiLeft;
    this.cornerY = this.guiTop;
    this.realWidth = this.xSize;
    this.realHeight = this.ySize;

    for (ModuleScreen<?,?> module : this.modules) {
      this.updateSubmodule(module);
    }
  }

  @Override
  public void init(Minecraft mc, int width, int height) {
    super.init(mc, width, height);

    for (ModuleScreen<?,?> module : this.modules) {
      module.init(mc, width, height);
      this.updateSubmodule(module);
    }
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
    for (ModuleScreen<?,?> module : this.modules) {
      module.handleDrawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
    this.drawContainerName(matrixStack);
    this.drawPlayerInventoryName(matrixStack);

    for (ModuleScreen<?,?> module : this.modules) {
      module.handleDrawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
    }
  }

  @Override
  protected void renderHoveredTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
    super.renderHoveredTooltip(matrixStack, mouseX, mouseY);

    for (ModuleScreen<?,?> module : this.modules) {
      module.handleRenderHoveredTooltip(matrixStack, mouseX, mouseY);
    }
  }

  protected void drawBackground(MatrixStack matrixStack, ResourceLocation background) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.minecraft.getTextureManager().bindTexture(background);
    this.blit(matrixStack, this.cornerX, this.cornerY, 0, 0, this.realWidth, this.realHeight);
  }

  protected void drawContainerName(MatrixStack matrixStack) {
    this.font.func_238422_b_(matrixStack, this.getTitle().func_241878_f(), 8, 6, 0x404040);
  }

  protected void drawPlayerInventoryName(MatrixStack matrixStack) {
    assert Minecraft.getInstance().player != null;
    ITextComponent localizedName = Minecraft.getInstance().player.inventory.getDisplayName();
    this.font.func_238422_b_(matrixStack, localizedName.func_241878_f(), 8, this.ySize - 96 + 2, 0x404040);
  }

  @Override
  public void resize(Minecraft mc, int width, int height) {
    super.resize(mc, width, height);

    for (ModuleScreen<?,?> module : this.modules) {
      module.resize(mc, width, height);
      this.updateSubmodule(module);
    }
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(matrixStack);
    int oldX = this.guiLeft;
    int oldY = this.guiTop;
    int oldW = this.xSize;
    int oldH = this.ySize;

    this.guiLeft = this.cornerX;
    this.guiTop = this.cornerY;
    this.xSize = this.realWidth;
    this.ySize = this.realHeight;
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    this.guiLeft = oldX;
    this.guiTop = oldY;
    this.xSize = oldW;
    this.ySize = oldH;
  }

  // needed to get the correct slot on clicking
  @Override
  protected boolean isPointInRegion(int left, int top, int right, int bottom, double pointX, double pointY) {
    pointX -= this.cornerX;
    pointY -= this.cornerY;
    return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
  }

  protected void updateSubmodule(ModuleScreen<?,?> module) {
    module.updatePosition(this.cornerX, this.cornerY, this.realWidth, this.realHeight);

    if (module.guiLeft < this.guiLeft) {
      this.xSize += this.guiLeft - module.guiLeft;
      this.guiLeft = module.guiLeft;
    }

    if (module.guiTop < this.guiTop) {
      this.ySize += this.guiTop - module.guiTop;
      this.guiTop = module.guiTop;
    }

    if (module.guiRight() > this.guiLeft + this.xSize) {
      this.xSize = module.guiRight() - this.guiLeft;
    }

    if (module.guiBottom() > this.guiTop + this.ySize) {
      this.ySize = module.guiBottom() - this.guiTop;
    }
  }

  @Override
  public void moveItems(MatrixStack matrixStack, Slot slotIn) {
    ModuleScreen<?,?> module = this.getModuleForSlot(slotIn.slotNumber);

    if (module != null) {
      Slot slot = slotIn;
      // unwrap for the call to the module
      if (slotIn instanceof WrapperSlot) {
        slot = ((WrapperSlot) slotIn).parent;
      }

      if (!module.shouldDrawSlot(slot)) {
        return;
      }
    }

    // update slot positions
    if (slotIn instanceof WrapperSlot) {
      slotIn.xPos = ((WrapperSlot) slotIn).parent.xPos;
      slotIn.yPos = ((WrapperSlot) slotIn).parent.yPos;
    }

    super.moveItems(matrixStack, slotIn);
  }

  @Override
  public boolean isSlotSelected(Slot slotIn, double mouseX, double mouseY) {
    ModuleScreen<?,?> module = this.getModuleForSlot(slotIn.slotNumber);

    // mouse inside the module of the slot?
    if (module != null) {
      Slot slot = slotIn;
      // unwrap for the call to the module
      if (slotIn instanceof WrapperSlot) {
        slot = ((WrapperSlot) slotIn).parent;
      }

      if (!module.shouldDrawSlot(slot)) {
        return false;
      }
    }

    return super.isSlotSelected(slotIn, mouseX, mouseY);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    ModuleScreen<?,?> module = this.getModuleForPoint(mouseX, mouseY);

    if (module != null) {
      if (module.handleMouseClicked(mouseX, mouseY, mouseButton)) {
        return false;
      }
    }

    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLastClick, double unkowwn) {
    ModuleScreen<?,?> module = this.getModuleForPoint(mouseX, mouseY);

    if (module != null) {
      if (module.handleMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
        return false;
      }
    }

    return super.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick, unkowwn);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    ModuleScreen<?,?> module = this.getModuleForPoint(mouseX, mouseY);

    if (module != null) {
      if (module.handleMouseScrolled(mouseX, mouseY, delta)) {
        return false;
      }
    }

    return super.mouseScrolled(mouseX, mouseY, delta);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int state) {
    ModuleScreen<?,?> module = this.getModuleForPoint(mouseX, mouseY);

    if (module != null) {
      if (module.handleMouseReleased(mouseX, mouseY, state)) {
        return false;
      }
    }

    return super.mouseReleased(mouseX, mouseY, state);
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForPoint(double x, double y) {
    for (ModuleScreen<?,?> module : this.modules) {
      if (this.isPointInRegion(module.guiLeft, module.guiTop, module.guiRight(), module.guiBottom(), x + this.cornerX, y + this.cornerY)) {
        return module;
      }
    }

    return null;
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForSlot(int slotNumber) {
    return this.getModuleForContainer(this.getContainer().getSlotContainer(slotNumber));
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForContainer(Container container) {
    for (ModuleScreen<?,?> module : this.modules) {
      if (module.getContainer() == container) {
        return module;
      }
    }

    return null;
  }

  @Override
  public CONTAINER getContainer() {
    return this.container;
  }
}

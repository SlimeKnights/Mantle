package slimeknights.mantle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import slimeknights.mantle.inventory.MultiModuleContainerMenu;
import slimeknights.mantle.inventory.WrapperSlot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiModuleScreen<CONTAINER extends MultiModuleContainerMenu<?>> extends AbstractContainerScreen<CONTAINER> {

  protected List<ModuleScreen<?,?>> modules = Lists.newArrayList();

  public int cornerX;
  public int cornerY;
  public int realWidth;
  public int realHeight;

  public MultiModuleScreen(CONTAINER container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);

    this.realWidth = -1;
    this.realHeight = -1;
    this.passEvents = true;
  }

  protected void addModule(ModuleScreen<?,?> module) {
    this.modules.add(module);
  }

  public List<Rect2i> getModuleAreas() {
    List<Rect2i> areas = new ArrayList<>(this.modules.size());
    for (ModuleScreen<?,?> module : this.modules) {
      areas.add(module.getArea());
    }
    return areas;
  }

  @Override
  protected void init() {
    if (this.realWidth > -1) {
      // has to be reset before calling initGui so the position is getting retained
      this.imageWidth = this.realWidth;
      this.imageHeight = this.realHeight;
    }

    super.init();

    this.cornerX = this.leftPos;
    this.cornerY = this.topPos;
    this.realWidth = this.imageWidth;
    this.realHeight = this.imageHeight;

    assert this.minecraft != null;
    for (ModuleScreen<?,?> module : this.modules) {
      this.updateSubmodule(module);
    }
    // TODO: this is a small ordering change, does it need another hook?
    for (ModuleScreen<?,?> module : this.modules) {
      module.init(this.minecraft, width, height);
      this.updateSubmodule(module);
    }
  }

//  @Override
//  public void init(Minecraft mc, int width, int height) {
//    super.init(mc, width, height);
//
//    for (ModuleScreen<?,?> module : this.modules) {
//      module.init(mc, width, height);
//      this.updateSubmodule(module);
//    }
//  }

  @Override
  protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
    for (ModuleScreen<?,?> module : this.modules) {
      module.handleDrawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
    }
  }

  @Override
  protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
    this.drawContainerName(matrixStack);
    this.drawPlayerInventoryName(matrixStack);

    for (ModuleScreen<?,?> module : this.modules) {
      // set correct state for the module
      matrixStack.pushPose();
      matrixStack.translate(module.leftPos - this.leftPos, module.topPos - this.topPos, 0.0F);
      module.handleDrawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
      matrixStack.popPose();
    }
  }

  @Override
  protected void renderTooltip(PoseStack matrixStack, int mouseX, int mouseY) {
    super.renderTooltip(matrixStack, mouseX, mouseY);

    for (ModuleScreen<?,?> module : this.modules) {
      module.handleRenderHoveredTooltip(matrixStack, mouseX, mouseY);
    }
  }

  protected void drawBackground(PoseStack matrixStack, ResourceLocation background) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, background);
    this.blit(matrixStack, this.cornerX, this.cornerY, 0, 0, this.realWidth, this.realHeight);
  }

  protected void drawContainerName(PoseStack matrixStack) {
    this.font.draw(matrixStack, this.getTitle().getVisualOrderText(), 8, 6, 0x404040);
  }

  protected void drawPlayerInventoryName(PoseStack matrixStack) {
    assert Minecraft.getInstance().player != null;
    Component localizedName = Minecraft.getInstance().player.getInventory().getDisplayName();
    this.font.draw(matrixStack, localizedName.getVisualOrderText(), 8, this.imageHeight - 96 + 2, 0x404040);
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
  public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(matrixStack);
    int oldX = this.leftPos;
    int oldY = this.topPos;
    int oldW = this.imageWidth;
    int oldH = this.imageHeight;

    this.leftPos = this.cornerX;
    this.topPos = this.cornerY;
    this.imageWidth = this.realWidth;
    this.imageHeight = this.realHeight;
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    this.renderTooltip(matrixStack, mouseX, mouseY);
    this.leftPos = oldX;
    this.topPos = oldY;
    this.imageWidth = oldW;
    this.imageHeight = oldH;
  }

  // needed to get the correct slot on clicking
  @Override
  protected boolean isHovering(int left, int top, int right, int bottom, double pointX, double pointY) {
    pointX -= this.cornerX;
    pointY -= this.cornerY;
    return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
  }

  protected void updateSubmodule(ModuleScreen<?,?> module) {
    module.updatePosition(this.cornerX, this.cornerY, this.realWidth, this.realHeight);

    if (module.leftPos < this.leftPos) {
      this.imageWidth += this.leftPos - module.leftPos;
      this.leftPos = module.leftPos;
    }

    if (module.topPos < this.topPos) {
      this.imageHeight += this.topPos - module.topPos;
      this.topPos = module.topPos;
    }

    if (module.guiRight() > this.leftPos + this.imageWidth) {
      this.imageWidth = module.guiRight() - this.leftPos;
    }

    if (module.guiBottom() > this.topPos + this.imageHeight) {
      this.imageHeight = module.guiBottom() - this.topPos;
    }
  }

  @Override
  public void renderSlot(PoseStack matrixStack, Slot slotIn) {
    ModuleScreen<?,?> module = this.getModuleForSlot(slotIn.index);

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
      slotIn.x = ((WrapperSlot) slotIn).parent.x;
      slotIn.y = ((WrapperSlot) slotIn).parent.y;
    }

    super.renderSlot(matrixStack, slotIn);
  }

  @Override
  public boolean isHovering(Slot slotIn, double mouseX, double mouseY) {
    ModuleScreen<?,?> module = this.getModuleForSlot(slotIn.index);

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

    return super.isHovering(slotIn, mouseX, mouseY);
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
      if (this.isHovering(module.leftPos, module.topPos, module.guiRight(), module.guiBottom(), x + this.cornerX, y + this.cornerY)) {
        return module;
      }
    }

    return null;
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForSlot(int slotNumber) {
    return this.getModuleForContainer(this.getMenu().getSlotContainer(slotNumber));
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForContainer(AbstractContainerMenu container) {
    for (ModuleScreen<?,?> module : this.modules) {
      if (module.getMenu() == container) {
        return module;
      }
    }

    return null;
  }

  @Override
  public CONTAINER getMenu() {
    return this.menu;
  }
}

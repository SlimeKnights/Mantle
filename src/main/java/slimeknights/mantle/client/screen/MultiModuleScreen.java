package slimeknights.mantle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.Rect2i;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import slimeknights.mantle.inventory.MultiModuleContainer;
import slimeknights.mantle.inventory.WrapperSlot;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiModuleScreen<CONTAINER extends MultiModuleContainer<?>> extends HandledScreen<CONTAINER> {

  protected List<ModuleScreen<?,?>> modules = Lists.newArrayList();

  public int cornerX;
  public int cornerY;
  public int realWidth;
  public int realHeight;

  public MultiModuleScreen(CONTAINER container, PlayerInventory playerInventory, Text title) {
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
  public void init() {
    if (this.realWidth > -1) {
      // has to be reset before calling initGui so the position is getting retained
      this.backgroundWidth = this.realWidth;
      this.backgroundHeight = this.realHeight;
    }

    super.init();

    this.cornerX = this.x;
    this.cornerY = this.y;
    this.realWidth = this.backgroundWidth;
    this.realHeight = this.backgroundHeight;

    for (ModuleScreen<?,?> module : this.modules) {
      this.updateSubmodule(module);
    }
  }

  @Override
  public void init(MinecraftClient mc, int width, int height) {
    super.init(mc, width, height);

    for (ModuleScreen<?,?> module : this.modules) {
      module.init(mc, width, height);
      this.updateSubmodule(module);
    }
  }

  @Override
  protected void drawBackground(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
    for (ModuleScreen<?,?> module : this.modules) {
      module.handleDrawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
    }
  }

  @Override
  protected void drawForeground(MatrixStack matrixStack, int mouseX, int mouseY) {
    this.drawContainerName(matrixStack);
    this.drawPlayerInventoryName(matrixStack);

    for (ModuleScreen<?,?> module : this.modules) {
      // set correct state for the module
      matrixStack.push();
      matrixStack.translate(module.x - this.x, module.y - this.y, 0.0F);
      module.handleDrawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
      matrixStack.pop();
    }
  }

  @Override
  protected void drawMouseoverTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
    super.drawMouseoverTooltip(matrixStack, mouseX, mouseY);

    for (ModuleScreen<?,?> module : this.modules) {
      module.handleRenderHoveredTooltip(matrixStack, mouseX, mouseY);
    }
  }

  protected void drawBackground(MatrixStack matrixStack, Identifier background) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.client.getTextureManager().bindTexture(background);
    this.drawTexture(matrixStack, this.cornerX, this.cornerY, 0, 0, this.realWidth, this.realHeight);
  }

  protected void drawContainerName(MatrixStack matrixStack) {
    this.textRenderer.draw(matrixStack, this.getTitle().asOrderedText(), 8, 6, 0x404040);
  }

  protected void drawPlayerInventoryName(MatrixStack matrixStack) {
    assert MinecraftClient.getInstance().player != null;
    Text localizedName = MinecraftClient.getInstance().player.inventory.getDisplayName();
    this.textRenderer.draw(matrixStack, localizedName.asOrderedText(), 8, this.backgroundHeight - 96 + 2, 0x404040);
  }

  @Override
  public void resize(MinecraftClient mc, int width, int height) {
    super.resize(mc, width, height);

    for (ModuleScreen<?,?> module : this.modules) {
      module.resize(mc, width, height);
      this.updateSubmodule(module);
    }
  }

  @Override
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(matrixStack);
    int oldX = this.x;
    int oldY = this.y;
    int oldW = this.backgroundWidth;
    int oldH = this.backgroundHeight;

    this.x = this.cornerX;
    this.y = this.cornerY;
    this.backgroundWidth = this.realWidth;
    this.backgroundHeight = this.realHeight;
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    this.drawMouseoverTooltip(matrixStack, mouseX, mouseY);
    this.x = oldX;
    this.y = oldY;
    this.backgroundWidth = oldW;
    this.backgroundHeight = oldH;
  }

  // needed to get the correct slot on clicking
  @Override
  protected boolean isPointWithinBounds(int left, int top, int right, int bottom, double pointX, double pointY) {
    pointX -= this.cornerX;
    pointY -= this.cornerY;
    return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
  }

  protected void updateSubmodule(ModuleScreen<?,?> module) {
    module.updatePosition(this.cornerX, this.cornerY, this.realWidth, this.realHeight);

    if (module.x < this.x) {
      this.backgroundWidth += this.x - module.x;
      this.x = module.x;
    }

    if (module.y < this.y) {
      this.backgroundHeight += this.y - module.y;
      this.y = module.y;
    }

    if (module.guiRight() > this.x + this.backgroundWidth) {
      this.backgroundWidth = module.guiRight() - this.x;
    }

    if (module.guiBottom() > this.y + this.backgroundHeight) {
      this.backgroundHeight = module.guiBottom() - this.y;
    }
  }

  @Override
  public void drawSlot(MatrixStack matrixStack, Slot slotIn) {
    ModuleScreen<?,?> module = this.getModuleForSlot(slotIn.id);

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

    super.drawSlot(matrixStack, slotIn);
  }

  @Override
  public boolean isPointOverSlot(Slot slotIn, double mouseX, double mouseY) {
    ModuleScreen<?,?> module = this.getModuleForSlot(slotIn.id);

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

    return super.isPointOverSlot(slotIn, mouseX, mouseY);
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
      if (this.isPointWithinBounds(module.x, module.y, module.guiRight(), module.guiBottom(), x + this.cornerX, y + this.cornerY)) {
        return module;
      }
    }

    return null;
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForSlot(int slotNumber) {
    return this.getModuleForContainer(this.getScreenHandler().getSlotContainer(slotNumber));
  }

  @Nullable
  protected ModuleScreen<?,?> getModuleForContainer(ScreenHandler container) {
    for (ModuleScreen<?,?> module : this.modules) {
      if (module.getScreenHandler() == container) {
        return module;
      }
    }

    return null;
  }

  @Override
  public CONTAINER getScreenHandler() {
    return this.handler;
  }
}

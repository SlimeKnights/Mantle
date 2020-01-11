package slimeknights.mantle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
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

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MultiModuleScreen<T extends MultiModuleContainer> extends ContainerScreen<T> {

  protected List<ModuleScreen> modules = Lists.newArrayList();

  public int cornerX;
  public int cornerY;
  public int realWidth;
  public int realHeight;

  public MultiModuleScreen(T container, PlayerInventory playerInventory, ITextComponent title) {
    super(container, playerInventory, title);

    this.realWidth = -1;
    this.realHeight = -1;
  }

  protected void addModule(ModuleScreen module) {
    this.modules.add(module);
  }

  public List<Rectangle2d> getModuleAreas() {
    List<Rectangle2d> areas = new ArrayList<Rectangle2d>(this.modules.size());
    for (ModuleScreen module : this.modules) {
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

    for (ModuleScreen module : this.modules) {
      this.updateSubmodule(module);
    }

    //this.guiLeft = this.guiTop = 0;
    //this.xSize = width;
    //this.ySize = height;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    for (ModuleScreen module : this.modules) {
      module.handleDrawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    this.drawContainerName();
    this.drawPlayerInventoryName();

    for (ModuleScreen module : this.modules) {
      // set correct state for the module
      RenderSystem.pushMatrix();
      RenderSystem.translatef(-this.guiLeft, -this.guiTop, 0.0F);
      RenderSystem.translatef(module.guiLeft, module.guiTop, 0.0F);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      module.handleDrawGuiContainerForegroundLayer(mouseX, mouseY);
      RenderSystem.popMatrix();
    }
  }

  protected void drawBackground(ResourceLocation background) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.minecraft.getTextureManager().bindTexture(background);
    this.blit(this.cornerX, this.cornerY, 0, 0, this.realWidth, this.realHeight);
  }

  protected void drawContainerName() {
    String localizedName = this.getTitle().getFormattedText();//multiContainer.getInventoryDisplayName();
    if (localizedName != null) {
      this.font.drawString(localizedName, 8, 6, 0x404040);
    }
  }

  protected void drawPlayerInventoryName() {
    String localizedName = Minecraft.getInstance().player.inventory.getDisplayName().getUnformattedComponentText();
    this.font.drawString(localizedName, 8, this.ySize - 96 + 2, 0x404040);
  }

  @Override
  public void init(Minecraft mc, int width, int height) {
    super.init(mc, width, height);

    for (ModuleScreen module : this.modules) {
      module.init(mc, width, height);
      this.updateSubmodule(module);
    }
  }

  @Override
  public void resize(@Nonnull Minecraft mc, int width, int height) {
    super.resize(mc, width, height);

    for (ModuleScreen module : this.modules) {
      module.resize(mc, width, height);
      this.updateSubmodule(module);
    }
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    this.renderBackground();
    int oldX = this.guiLeft;
    int oldY = this.guiTop;
    int oldW = this.xSize;
    int oldH = this.ySize;

    this.guiLeft = this.cornerX;
    this.guiTop = this.cornerY;
    this.xSize = this.realWidth;
    this.ySize = this.realHeight;
    super.render(mouseX, mouseY, partialTicks);
    this.renderHoveredToolTip(mouseX, mouseY);
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

  protected void updateSubmodule(ModuleScreen module) {
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
  public void drawSlot(Slot slotIn) {
    ModuleScreen module = this.getModuleForSlot(slotIn.slotNumber);

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

    super.drawSlot(slotIn);
  }

  @Override
  public boolean isSlotSelected(Slot slotIn, double mouseX, double mouseY) {
    ModuleScreen module = this.getModuleForSlot(slotIn.slotNumber);

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
    ModuleScreen module = this.getModuleForPoint(mouseX, mouseY);
    if (module != null) {
      if (module.handleMouseClicked(mouseX, mouseY, mouseButton)) {
        return false;
      }
    }
    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLastClick, double unkowwn) {
    ModuleScreen module = this.getModuleForPoint(mouseX, mouseY);
    if (module != null) {
      if (module.handleMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
        return false;
      }
    }

    return super.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick, unkowwn);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int state) {
    ModuleScreen module = this.getModuleForPoint(mouseX, mouseY);
    if (module != null) {
      if (module.handleMouseReleased(mouseX, mouseY, state)) {
        return false;
      }
    }

    return super.mouseReleased(mouseX, mouseY, state);
  }

  protected ModuleScreen getModuleForPoint(double x, double y) {
    for (ModuleScreen module : this.modules) {
      if (this.isPointInRegion(module.guiLeft, module.guiTop, module.guiRight(), module.guiBottom(),
              x + this.cornerX, y + this.cornerY)) {
        return module;
      }
    }

    return null;
  }

  protected ModuleScreen getModuleForSlot(int slotNumber) {
    return this.getModuleForContainer(this.getContainer().getSlotContainer(slotNumber));
  }

  protected ModuleScreen getModuleForContainer(Container container) {
    for (ModuleScreen module : this.modules) {
      if (module.getContainer() == container) {
        return module;
      }
    }

    return null;
  }

  @Nonnull
  @Override
  public T getContainer() {
    return this.container;
  }

}

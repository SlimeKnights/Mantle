package slimeknights.mantle.client.gui;

import com.google.common.collect.Lists;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import slimeknights.mantle.Mantle;
import slimeknights.mantle.inventory.ContainerMultiModule;
import slimeknights.mantle.inventory.SlotWrapper;

@OnlyIn(Dist.CLIENT)
public class GuiMultiModule extends ContainerScreen
{

  // NEI-stuff >:(
  private static Field NEI_Manager;

  static {
    try {
      NEI_Manager = ContainerScreen.class.getDeclaredField("manager");
    } catch(NoSuchFieldException e) {
      NEI_Manager = null;
    }
  }

  protected List<GuiModule> modules = Lists.newArrayList();

  public int cornerX;
  public int cornerY;
  public int realWidth;
  public int realHeight;

  public GuiMultiModule(ContainerMultiModule container) {
    super(container);

    realWidth = -1;
    realHeight = -1;
  }

  protected void addModule(GuiModule module) {
    modules.add(module);
  }

  public List<Rectangle2d> getModuleAreas() {
    List<Rectangle2d> areas = new ArrayList<Rectangle2d>(modules.size());
    for(GuiModule module : modules) {
      areas.add(module.getArea());
    }
    return areas;
  }

  @Override
  public void initGui() {
    if(realWidth > -1) {
      // has to be reset before calling initGui so the position is getting retained
      xSize = realWidth;
      ySize = realHeight;
    }
    super.initGui();

    this.cornerX = this.guiLeft;
    this.cornerY = this.guiTop;
    this.realWidth = xSize;
    this.realHeight = ySize;

    for(GuiModule module : modules) {
      updateSubmodule(module);
    }

    //this.guiLeft = this.guiTop = 0;
    //this.xSize = width;
    //this.ySize = height;
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    for(GuiModule module : modules) {
      module.handleDrawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    drawContainerName();
    drawPlayerInventoryName();

    for(GuiModule module : modules) {
      // set correct state for the module
      GlStateManager.pushMatrix();
      GlStateManager.translatef(-this.guiLeft, -this.guiTop, 0.0F);
      GlStateManager.translatef(module.guiLeft, module.guiTop, 0.0F);
      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      module.handleDrawGuiContainerForegroundLayer(mouseX, mouseY);
      GlStateManager.popMatrix();
    }
  }

  protected void drawBackground(ResourceLocation background) {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(background);
    this.drawTexturedModalRect(cornerX, cornerY, 0, 0, realWidth, realHeight);
  }

  protected void drawContainerName() {
    ContainerMultiModule multiContainer = (ContainerMultiModule) this.inventorySlots;
    String localizedName = multiContainer.getInventoryDisplayName();
    if(localizedName != null) {
      this.fontRenderer.drawString(localizedName, 8, 6, 0x404040);
    }
  }

  protected void drawPlayerInventoryName() {
    String localizedName = Minecraft.getInstance().player.inventory.getDisplayName().getUnformattedComponentText();
    this.fontRenderer.drawString(localizedName, 8, this.ySize - 96 + 2, 0x404040);
  }

  @Override
  public void setWorldAndResolution(Minecraft mc, int width, int height) {
    super.setWorldAndResolution(mc, width, height);

    // workaround for NEIs ASM hax. sigh.
    try {
      for(GuiModule module : modules) {
        module.setWorldAndResolution(mc, width, height);
        if(NEI_Manager != null) {
          NEI_Manager.set(module, NEI_Manager.get(this));
        }
        updateSubmodule(module);
      }
    } catch(IllegalAccessException e) {
      Mantle.logger.error(e);
    }
  }

  @Override
  public void onResize(@Nonnull Minecraft mc, int width, int height) {
    super.onResize(mc, width, height);

    for(GuiModule module : modules) {
      module.onResize(mc, width, height);
      updateSubmodule(module);
    }
  }

  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    this.drawDefaultBackground();
    int oldX = guiLeft;
    int oldY = guiTop;
    int oldW = xSize;
    int oldH = ySize;

    guiLeft = cornerX;
    guiTop = cornerY;
    xSize = realWidth;
    ySize = realHeight;
    super.render(mouseX, mouseY, partialTicks);
    this.renderHoveredToolTip(mouseX, mouseY);
    guiLeft = oldX;
    guiTop = oldY;
    xSize = oldW;
    ySize = oldH;
  }


  // needed to get the correct slot on clicking
  @Override
  protected boolean isPointInRegion(int left, int top, int right, int bottom, double pointX, double pointY) {
    pointX -= this.cornerX;
    pointY -= this.cornerY;
    return pointX >= left - 1 && pointX < left + right + 1 && pointY >= top - 1 && pointY < top + bottom + 1;
  }

  protected void updateSubmodule(GuiModule module) {
    module.updatePosition(this.cornerX, this.cornerY, this.realWidth, this.realHeight);

    if(module.guiLeft < this.guiLeft) {
      this.xSize += this.guiLeft - module.guiLeft;
      this.guiLeft = module.guiLeft;
    }
    if(module.guiTop < this.guiTop) {
      this.ySize += this.guiTop - module.guiTop;
      this.guiTop = module.guiTop;
    }
    if(module.guiRight() > this.guiLeft + this.xSize) {
      xSize = module.guiRight() - this.guiLeft;
    }
    if(module.guiBottom() > this.guiTop + this.ySize) {
      ySize = module.guiBottom() - this.guiTop;
    }
  }

  @Override
  public void drawSlot(Slot slotIn) {
    GuiModule module = getModuleForSlot(slotIn.slotNumber);

    if(module != null) {
      Slot slot = slotIn;
      // unwrap for the call to the module
      if(slotIn instanceof SlotWrapper) {
        slot = ((SlotWrapper) slotIn).parent;
      }
      if(!module.shouldDrawSlot(slot)) {
        return;
      }
    }

    // update slot positions
    if(slotIn instanceof SlotWrapper) {
      slotIn.xPos = ((SlotWrapper) slotIn).parent.xPos;
      slotIn.yPos = ((SlotWrapper) slotIn).parent.yPos;
    }

    super.drawSlot(slotIn);
  }

  @Override
  public boolean isSlotSelected(Slot slotIn, double mouseX, double mouseY) {
    GuiModule module = getModuleForSlot(slotIn.slotNumber);

    // mouse inside the module of the slot?
    if(module != null) {
      Slot slot = slotIn;
      // unwrap for the call to the module
      if(slotIn instanceof SlotWrapper) {
        slot = ((SlotWrapper) slotIn).parent;
      }
      if(!module.shouldDrawSlot(slot)) {
        return false;
      }
    }

    return super.isSlotSelected(slotIn, mouseX, mouseY);
  }


  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    GuiModule module = getModuleForPoint(mouseX, mouseY);
    if(module != null) {
      if(module.handleMouseClicked(mouseX, mouseY, mouseButton)) {
        return false;
      }
    }
    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double timeSinceLastClick, double unkowwn) {
    GuiModule module = getModuleForPoint(mouseX, mouseY);
    if(module != null) {
      if(module.handleMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
        return false;
      }
    }

    return super.mouseDragged(mouseX, mouseY, clickedMouseButton, timeSinceLastClick, unkowwn);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int state) {
    GuiModule module = getModuleForPoint(mouseX, mouseY);
    if(module != null) {
      if(module.handleMouseReleased(mouseX, mouseY, state)) {
        return false;
      }
    }

    return super.mouseReleased(mouseX, mouseY, state);
  }

  protected GuiModule getModuleForPoint(double x, double y) {
    for(GuiModule module : modules) {
      if(this.isPointInRegion(module.guiLeft, module.guiTop, module.guiRight(), module.guiBottom(),
                              x + this.cornerX, y + this.cornerY)) {
        return module;
      }
    }

    return null;
  }

  protected GuiModule getModuleForSlot(int slotNumber) {
    return getModuleForContainer(getContainer().getSlotContainer(slotNumber));
  }

  protected GuiModule getModuleForContainer(Container container) {
    for(GuiModule module : modules) {
      if(module.inventorySlots == container) {
        return module;
      }
    }

    return null;
  }

  protected ContainerMultiModule getContainer() {
    return (ContainerMultiModule) inventorySlots;
  }
}

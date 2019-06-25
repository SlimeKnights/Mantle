package slimeknights.mantle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class WidgetTabs extends Widget {

  private static final ResourceLocation
          creativeInventoryTabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

  private final ElementScreen[] tabActive = new ElementScreen[3];
  private final ElementScreen[] tab = new ElementScreen[3];

  // change this if you need different tabs
  public ResourceLocation tabsResource = creativeInventoryTabs;
  // changes the y-offset of the tab row
  public int yOffset = 4;
  // space between 2 tabs
  public int spacing = 2;

  public int selected;
  public int highlighted;
  protected List<ItemStack> icons = Lists.newArrayList();

  private final MultiModuleScreen parent;
  private boolean clicked = false;

  public WidgetTabs(MultiModuleScreen parent, ElementScreen tabLeft, ElementScreen tabCenter, ElementScreen tabRight, ElementScreen activeLeft, ElementScreen activeCenter, ElementScreen activeRight) {
    this.parent = parent;

    this.tab[0] = tabLeft;
    this.tab[1] = tabCenter;
    this.tab[2] = tabRight;
    this.tabActive[0] = activeLeft;
    this.tabActive[1] = activeCenter;
    this.tabActive[2] = activeRight;

    this.selected = 0;
  }

  public void addTab(ItemStack icon) {
    this.icons.add(icon);
  }

  public void clear() {
    this.selected = 0;
    this.icons.clear();
  }

  public void update(int mouseX, int mouseY) {
    boolean mouseDown = Minecraft.getInstance().mouseHelper.isLeftDown(); // left mouse button

    // did we click on a tab?
    mouseX -= this.xPos;
    mouseY -= this.yPos;

    // update highlighted
    this.highlighted = -1;
    if (mouseY >= 0 && mouseY <= this.tab[1].h) {
      // which one did we click?
      int x = 0;
      for (int i = 0; i < this.icons.size(); i++) {
        // clicking on spacing has no effect
        if (mouseX >= x && mouseX < x + this.tab[1].w) {
          this.highlighted = i;
          break;
        }
        x += this.tab[1].w;
        x += this.spacing;
      }
    }

    // already clicked
    if (this.clicked) {
      // still clicking
      if (mouseDown) {
        return;
      }
      // release click
      else {
        this.clicked = false;
        return;
      }
    }
    // new click
    else if (mouseDown) {
      this.clicked = true;
    }
    // no click - do nothing
    else {
      return;
    }

    // was new click, select highlighted
    if (this.highlighted > -1) {
      this.selected = this.highlighted;
    }
  }

  @Override
  public void draw() {
    int y = this.yPos + this.yOffset;
    for (int i = 0; i < this.icons.size(); i++) {
      int x = this.xPos + i * this.tab[0].w;

      if (i > 0) {
        x += i * this.spacing;
      }

      ElementScreen[] toDraw;
      if (i == this.selected) {
        toDraw = this.tabActive;
      }
      else {
        toDraw = this.tab;
      }

      ElementScreen actualTab;
      if (i == 0 && x == this.parent.cornerX) {
        actualTab = toDraw[0];
      }
      else if (x == this.parent.cornerX + this.parent.width) {
        actualTab = toDraw[2];
      }
      else {
        actualTab = toDraw[1];
      }

      // todo: draw all the tabs first and then all the itemstacks so it doesn't have to switch texture in between all the time

      // rebind texture from drawing an itemstack
      Minecraft.getInstance().getTextureManager().bindTexture(this.tabsResource);
      actualTab.draw(x, y);

      ItemStack icon = this.icons.get(i);
      if (icon != null) {
        RenderHelper.enableGUIStandardItemLighting();
        this.drawItemStack(icon, x + (actualTab.w - 16) / 2, y + (actualTab.h - 16) / 2);
        RenderHelper.disableStandardItemLighting();
        //RenderHelper.enableStandardItemLighting();
      }
    }
  }

  // guiContainer.drawItemStack
  private void drawItemStack(ItemStack stack, int x, int y) {
    ItemRenderer itemRender = Minecraft.getInstance().getItemRenderer();
    GlStateManager.translatef(0.0F, 0.0F, 32.0F);
    itemRender.zLevel = 200;
    //FontRenderer font = stack.getItem().getFontRenderer(stack);

    itemRender.renderItemAndEffectIntoGUI(stack, x, y);
    //this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y - (this.draggedStack == null ? 0 : 8), altText);
    //this.zLevel = 0.0F;
    itemRender.zLevel = 0.0F;
  }
}

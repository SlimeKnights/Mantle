package slimeknights.mantle.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public class TabsWidget extends Widget {

  private static final Identifier creativeInventoryTabs = new Identifier("textures/gui/container/creative_inventory/tabs.png");

  private final ElementScreen[] tabActive = new ElementScreen[3];
  private final ElementScreen[] tab = new ElementScreen[3];

  // change this if you need different tabs
  public Identifier tabsResource = creativeInventoryTabs;
  // changes the y-offset of the tab row
  public int yOffset = 4;
  // space between 2 tabs
  public int spacing = 2;

  public int selected;
  public int highlighted;
  protected List<ItemStack> icons = Lists.newArrayList();

  private final MultiModuleScreen parent;
  private boolean clicked = false;
  private boolean leftMouseDown = false;

  public TabsWidget(MultiModuleScreen parent, ElementScreen tabLeft, ElementScreen tabCenter, ElementScreen tabRight, ElementScreen activeLeft, ElementScreen activeCenter, ElementScreen activeRight) {
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

  @Override
  public void handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (mouseButton == 0) {
      this.leftMouseDown = true;
    }
  }

  @Override
  public void handleMouseReleased() {
    this.leftMouseDown = false;
  }

  public void update(int mouseX, int mouseY) {
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
      if (!this.leftMouseDown) {
        this.clicked = false;
      }
      return;
    }
    // new click
    else if (this.leftMouseDown) {
      this.clicked = true;
    }
    // no click - do nothing
    else {
      return;
    }

    // was a new click, select highlighted
    if (this.highlighted > -1) {
      this.selected = this.highlighted;
    }
  }

  @Override
  public void draw(MatrixStack matrixStack) {
    int y = this.yPos + this.yOffset;
    for (int i = 0; i < this.icons.size(); i++) {
      int x = this.xPos + i * this.tab[0].w;

      if (i > 0) {
        x += i * this.spacing;
      }

      ElementScreen[] toDraw;
      if (i == this.selected) {
        toDraw = this.tabActive;
      } else {
        toDraw = this.tab;
      }

      ElementScreen actualTab;
      if (i == 0 && x == this.parent.cornerX) {
        actualTab = toDraw[0];
      } else if (x == this.parent.cornerX + this.parent.width) {
        actualTab = toDraw[2];
      } else {
        actualTab = toDraw[1];
      }

      // todo: draw all the tabs first and then all the itemstacks so it doesn't have to switch texture in between all the time

      // rebind texture from drawing an itemstack
      MinecraftClient.getInstance().getTextureManager().bindTexture(this.tabsResource);
      actualTab.draw(matrixStack,x, y);

      ItemStack icon = this.icons.get(i);
      if (icon != null) {
        this.drawItemStack(icon, x + (actualTab.w - 16) / 2, y + (actualTab.h - 16) / 2);
        DiffuseLighting.disable();
      }
    }
  }

  // ContainerScreen.drawItemStack
  private void drawItemStack(ItemStack stack, int x, int y) {
    ItemRenderer itemRender = MinecraftClient.getInstance().getItemRenderer();
    RenderSystem.translatef(0.0F, 0.0F, 32.0F);
    itemRender.zOffset = 200.0F;

    itemRender.renderInGuiWithOverrides(stack, x, y);
    itemRender.zOffset = 0.0F;
  }
}

package slimeknights.mantle.client.gui.book.element;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ElementItem extends BookElement {

  public static final int ITEM_SIZE_HARDCODED = 16;
  public static final int ITEM_SWITCH_TICKS = 90;

  public ItemStack[] itemCycle;
  public float scale;

  public int renderTick = 0;
  public int currentItem = 0;

  public ElementItem(int x, int y, float scale, Item item) {
    this(x, y, scale, new ItemStack(item));
  }

  public ElementItem(int x, int y, float scale, Block item) {
    this(x, y, scale, new ItemStack(item));
  }

  public ElementItem(int x, int y, float scale, ItemStack item) {
    this(x, y, scale, new ItemStack[]{item});
  }

  public ElementItem(int x, int y, float scale, ItemStack[] itemCycle) {
    super(x, y);

    this.itemCycle = itemCycle;
    this.scale = scale;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks) {
    renderTick++;

    if (renderTick > ITEM_SWITCH_TICKS) {
      renderTick = 0;
      currentItem++;

      if (currentItem >= itemCycle.length)
        currentItem = 0;
    }

    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale(scale, scale, 1.0F);

    if (currentItem < itemCycle.length)
      Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(itemCycle[currentItem], 0, 0);

    GlStateManager.popMatrix();
  }

  @Override
  public void drawOverlay(int mouseX, int mouseY, float partialTicks) {
    if (mouseX >= x && mouseY >= y && mouseX <= x + ITEM_SIZE_HARDCODED * scale && mouseY <= y + ITEM_SIZE_HARDCODED * scale && currentItem < itemCycle.length) {
      renderToolTip(Minecraft.getMinecraft().fontRendererObj, itemCycle[currentItem], mouseX, mouseY);
    }
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (mouseButton == 0 && mouseX >= x && mouseY >= y && mouseX <= x + ITEM_SIZE_HARDCODED * scale && mouseY <= y + ITEM_SIZE_HARDCODED * scale && currentItem < itemCycle.length) {
      parent.itemClicked(itemCycle[currentItem]);
    }
  }
}

package slimeknights.mantle.client.gui.book.element;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.List;

import slimeknights.mantle.client.book.action.StringActionProcessor;

@SideOnly(Side.CLIENT)
public class ElementItem extends SizedBookElement {

  public static final int ITEM_SIZE_HARDCODED = 16;
  public static final int ITEM_SWITCH_TICKS = 90;

  public ItemStack[] itemCycle;
  public float scale;
  public String action;
  public List<String> tooltip;

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

  public ElementItem(int x, int y, float scale, Collection<ItemStack> itemCycle) {
    this(x, y, scale, itemCycle.toArray(new ItemStack[itemCycle.size()]));
  }

  public ElementItem(int x, int y, float scale, Collection<ItemStack> itemCycle, String action) {
    this(x, y, scale, itemCycle.toArray(new ItemStack[itemCycle.size()]), action);
  }

  public ElementItem(int x, int y, float scale, ItemStack... itemCycle) {
    this(x, y, scale, itemCycle, null);
  }

  public ElementItem(int x, int y, float scale, ItemStack[] itemCycle, String action) {
    super(x, y, MathHelper.floor_float(ITEM_SIZE_HARDCODED * scale), MathHelper.floor_float(ITEM_SIZE_HARDCODED * scale));

    this.itemCycle = itemCycle;
    this.scale = scale;
    this.action = action;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    renderTick++;

    if(renderTick > ITEM_SWITCH_TICKS) {
      renderTick = 0;
      currentItem++;

      if(currentItem >= itemCycle.length) {
        currentItem = 0;
      }
    }

    RenderHelper.enableGUIStandardItemLighting();
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale(scale, scale, 1.0F);

    if(currentItem < itemCycle.length) {
      mc.getRenderItem().renderItemAndEffectIntoGUI(itemCycle[currentItem], 0, 0);
    }

    GlStateManager.popMatrix();
  }

  @Override
  public void drawOverlay(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    if(isHovered(mouseX, mouseY) && currentItem < itemCycle.length) {
      if(tooltip != null) {
        drawHoveringText(tooltip, mouseX, mouseY, fontRenderer);
      }
      else {
        renderToolTip(fontRenderer, itemCycle[currentItem], mouseX, mouseY);
      }
    }
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if(mouseButton == 0 && isHovered(mouseX, mouseY) && currentItem < itemCycle.length) {
      if(action != null) {
        StringActionProcessor.process(action, parent);
      } else {
        parent.itemClicked(itemCycle[currentItem]);
      }
    }
  }
}

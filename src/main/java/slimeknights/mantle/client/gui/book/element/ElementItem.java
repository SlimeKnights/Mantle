package slimeknights.mantle.client.gui.book.element;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ElementItem extends BookElement {

  public static final int ITEM_SIZE_HARDCODED = 16;

  public ItemStack item;
  public float scale;

  public ElementItem(int x, int y, float scale, Item item) {
    this(x, y, scale, new ItemStack(item));
  }

  public ElementItem(int x, int y, float scale, Block item) {
    this(x, y, scale, new ItemStack(item));
  }

  public ElementItem(int x, int y, float scale, ItemStack item) {
    super(x, y);

    this.item = item;
    this.scale = scale;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, 0);
    GlStateManager.scale(scale, scale, 1.0F);

    Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(item, 0, 0);

    GlStateManager.popMatrix();

    if (mouseX >= x && mouseY >= y && mouseX <= x + ITEM_SIZE_HARDCODED * scale && mouseY <= y + ITEM_SIZE_HARDCODED * scale) {
      renderToolTip(Minecraft.getMinecraft().fontRendererObj, item, mouseX, mouseY);
    }
  }

  @Override
  public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    if (mouseButton == 0 && mouseX >= x && mouseY >= y && mouseX <= x + ITEM_SIZE_HARDCODED * scale && mouseY <= y + ITEM_SIZE_HARDCODED * scale) {
      parent.itemClicked(item);
    }
  }
}

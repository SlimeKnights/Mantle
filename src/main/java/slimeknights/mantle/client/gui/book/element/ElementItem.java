package slimeknights.mantle.client.gui.book.element;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ElementItem extends BookElement {

  public ItemStack item;

  public ElementItem(int x, int y, Item item) {
    this(x, y, new ItemStack(item));
  }

  public ElementItem(int x, int y, Block item) {
    this(x, y, new ItemStack(item));
  }

  public ElementItem(int x, int y, ItemStack item) {
    super(x, y);

    this.item = item;
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks) {
    Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(item, x, y);
  }
}

package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.client.book.action.StringActionProcessor;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ItemElement extends SizedBookElement {

  public static final int ITEM_SIZE_HARDCODED = 16;
  public static final int ITEM_SWITCH_TICKS = 90;

  public DefaultedList<ItemStack> itemCycle;
  public float scale;
  @Nullable
  public String action;
  public List<Text> tooltip;

  public int renderTick = 0;
  public int currentItem = 0;

  public ItemElement(int x, int y, float scale, Item item) {
    this(x, y, scale, new ItemStack(item));
  }

  public ItemElement(int x, int y, float scale, Block item) {
    this(x, y, scale, new ItemStack(item));
  }

  public ItemElement(int x, int y, float scale, ItemStack item) {
    this(x, y, scale, new ItemStack[] { item });
  }

  public ItemElement(int x, int y, float scale, Collection<ItemStack> itemCycle) {
    this(x, y, scale, itemCycle.toArray(new ItemStack[0]));
  }

  public ItemElement(int x, int y, float scale, Collection<ItemStack> itemCycle, String action) {
    this(x, y, scale, itemCycle.toArray(new ItemStack[0]), action);
  }

  public ItemElement(int x, int y, float scale, ItemStack... itemCycle) {
    this(x, y, scale, itemCycle, null);
  }

  public ItemElement(int x, int y, float scale, ItemStack[] itemCycle, @Nullable String action) {
    super(x, y, MathHelper.floor(ITEM_SIZE_HARDCODED * scale), MathHelper.floor(ITEM_SIZE_HARDCODED * scale));

    DefaultedList<ItemStack> nonNullStacks = DefaultedList.ofSize(itemCycle.length, ItemStack.EMPTY);
    for (int i = 0; i < itemCycle.length; i++) {
      if (!itemCycle[i].isEmpty()) {
        nonNullStacks.set(i, itemCycle[i].copy());
      }
    }

    this.itemCycle = nonNullStacks;
    this.scale = scale;
    this.action = action;
  }

  @Override
  public void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, TextRenderer fontRenderer) {
    this.renderTick++;

    if (this.renderTick > ITEM_SWITCH_TICKS) {
      this.renderTick = 0;
      this.currentItem++;

      if (this.currentItem >= this.itemCycle.size()) {
        this.currentItem = 0;
      }
    }

    RenderSystem.pushMatrix();
    RenderSystem.translatef(this.x, this.y, 0);
    RenderSystem.scalef(this.scale, this.scale, 1.0F);

    if (this.currentItem < this.itemCycle.size()) {
      this.mc.getItemRenderer().renderInGuiWithOverrides(this.itemCycle.get(this.currentItem), 0, 0);
    }

    RenderSystem.popMatrix();
    DiffuseLighting.disable();
  }

  @Override
  public void drawOverlay(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, TextRenderer fontRenderer) {
    if (this.isHovered(mouseX, mouseY) && this.currentItem < this.itemCycle.size()) {
      if (this.tooltip != null) {
        this.drawHoveringText(matrixStack, this.tooltip, mouseX, mouseY, fontRenderer);
      }
      else {
        this.renderToolTip(matrixStack, fontRenderer, this.itemCycle.get(this.currentItem), mouseX, mouseY);
      }
    }
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (mouseButton == 0 && this.isHovered(mouseX, mouseY) && this.currentItem < this.itemCycle.size()) {
      if (this.action != null) {
        StringActionProcessor.process(this.action, this.parent);
      }
      else {
        this.parent.itemClicked(this.itemCycle.get(this.currentItem));
      }
    }
  }
}

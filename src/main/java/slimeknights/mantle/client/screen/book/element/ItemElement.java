package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions.FontContext;
import slimeknights.mantle.client.book.action.StringActionProcessor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class ItemElement extends SizedBookElement {

  public static final int ITEM_SIZE_HARDCODED = 16;
  public static final long ITEM_SWITCH_TIME = 3000000000L; // 3 seconds

  public NonNullList<ItemStack> itemCycle;
  public float scale;
  @Nullable
  public String action;
  public List<Component> tooltip;

  public long lastTime;
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
    super(x, y, Mth.floor(ITEM_SIZE_HARDCODED * scale), Mth.floor(ITEM_SIZE_HARDCODED * scale));

    lastTime = Util.getNanos();

    NonNullList<ItemStack> nonNullStacks = NonNullList.withSize(itemCycle.length, ItemStack.EMPTY);
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
  public void draw(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    long nano = Util.getNanos();

    if(nano > lastTime + ITEM_SWITCH_TIME) {
      this.lastTime = nano;
      this.currentItem++;

      if (this.currentItem >= this.itemCycle.size()) {
        this.currentItem = 0;
      }
    }

    if (this.currentItem < this.itemCycle.size()) {
      // Lighting.turnBackOn(); TODO: still needed?

      matrixStack.pushPose();
      matrixStack.translate(x, y, 0);
      matrixStack.scale(scale, scale, 1.0F);

      // Matrix stack -> old system
      PoseStack poses = RenderSystem.getModelViewStack();
      poses.pushPose();
      poses.mulPoseMatrix(matrixStack.last().pose());

      ItemStack stack = this.itemCycle.get(this.currentItem);
      this.mc.getItemRenderer().renderAndDecorateItem(stack, 0, 0);
      Font font = IClientItemExtensions.of(stack).getFont(stack, FontContext.TOOLTIP);
      if (font == null) font = mc.font;
      this.mc.getItemRenderer().renderGuiItemDecorations(font, stack, 0, 0, null);

      matrixStack.popPose();
      poses.popPose();
      RenderSystem.applyModelViewMatrix();
      // Lighting.turnOff(); TODO: still needed?
    }
  }

  @Override
  public void drawOverlay(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    if (this.isHovered(mouseX, mouseY) && this.currentItem < this.itemCycle.size()) {
      if (this.tooltip != null) {
        this.drawTooltip(matrixStack, this.tooltip, mouseX, mouseY, fontRenderer);
      }
      else {
        this.renderToolTip(matrixStack, fontRenderer, this.itemCycle.get(this.currentItem), mouseX, mouseY);
      }
    }
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (mouseButton == 0 && this.isHovered(mouseX, mouseY) && this.currentItem < this.itemCycle.size()) {
      if (!StringUtil.isNullOrEmpty(this.action)) {
        StringActionProcessor.process(this.action, this.parent);
      }
    }
  }
}

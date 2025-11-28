package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

import static slimeknights.mantle.client.screen.book.element.ItemElement.ITEM_SIZE_HARDCODED;

/** Element that just draws a sprite from a texture atlas */
public class SpriteElement extends SizedBookElement {
  private final TextureAtlasSprite sprite;
  private float scale;

  public SpriteElement(int x, int y, float scale, TextureAtlasSprite sprite) {
    super(x, y, Mth.floor(ITEM_SIZE_HARDCODED * scale), Mth.floor(ITEM_SIZE_HARDCODED * scale));
    this.scale = scale;
    this.sprite = sprite;
  }

  public SpriteElement(int x, int y, float scale, ResourceLocation location) {
    this(x, y, scale, Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(location));
  }

  @Override
  public void scale(float scale) {
    this.scale = scale;
  }

  @Override
  public void draw(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    int x = this.x;
    int y = this.y;
    // if scaling, need to adjust pose stack
    if (scale != 1) {
      PoseStack matrices = graphics.pose();
      matrices.pushPose();
      // want to translate before scaling, so clear local variables
      matrices.translate(x, y, 0);
      x = 0;
      y = 0;
      matrices.scale(scale, scale, 1);
    }
    graphics.blit(x, y, 0, 16, 16, sprite);
    if (scale != 1) {
      graphics.pose().popPose();
    }
  }
}

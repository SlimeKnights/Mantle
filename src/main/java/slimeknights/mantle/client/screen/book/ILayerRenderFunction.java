package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import slimeknights.mantle.client.screen.book.element.BookElement;

public interface ILayerRenderFunction {
  void draw(BookElement element, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer);
}

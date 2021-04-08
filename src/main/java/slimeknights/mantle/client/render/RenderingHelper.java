package slimeknights.mantle.client.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.json.ModelTransformation.Mode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import net.minecraftforge.fml.client.gui.GuiUtils;
import slimeknights.mantle.client.model.inventory.ModelItem;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class RenderingHelper {
  /* Rotation */

  /**
   * Applies horizontal rotation to the given TESR
   * @param matrices  Matrix stack
   * @param state     Block state, checked for {@link Properties#HORIZONTAL_FACING}
   * @return  True if rotation was applied. Caller is expected to call {@link MatrixStack#pop()} if true
   */
  public static boolean applyRotation(MatrixStack matrices, BlockState state) {
    if (state.contains(Properties.HORIZONTAL_FACING)) {
      return applyRotation(matrices, state.get(Properties.HORIZONTAL_FACING));
    }
    return false;
  }

  /**
   * Applies horizontal rotation to the given TESR
   * @param matrices  Matrix stack
   * @param facing    Direction of rotation
   * @return  True if rotation was applied. Caller is expected to call {@link MatrixStack#pop()} if true
   */
  public static boolean applyRotation(MatrixStack matrices, Direction facing) {
    // south has a facing of 0, no rotation needed
    if (facing.getAxis().isHorizontal() && facing != Direction.SOUTH) {
      matrices.push();
      matrices.translate(0.5, 0, 0.5);
      matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-90f * (facing.getHorizontal())));
      matrices.translate(-0.5, 0, -0.5);
      return true;
    }
    return false;
  }


  /* Items */

  /**
   * Renders a single item in a TESR
   * @param matrices    Matrix stack inst ance
   * @param buffer      Buffer instance
   * @param item        Item to render
   * @param modelItem   Model items for render information
   * @param light       Model light
   */
  public static void renderItem(MatrixStack matrices, VertexConsumerProvider buffer, ItemStack item, ModelItem modelItem, int light) {
    // if the item says skip, skip
    if (modelItem.isHidden()) return;
    // if no stack, skip
    if (item.isEmpty()) return;

    // start rendering
    matrices.push();
    Vector3f center = modelItem.getCenterScaled();
    matrices.translate(center.getX(), center.getY(), center.getZ());

    // scale
    float scale = modelItem.getSizeScaled();
    matrices.scale(scale, scale, scale);

    // rotate X, then Y
    float x = modelItem.getX();
    if (x != 0) {
      matrices.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(x));
    }
    float y = modelItem.getY();
    if (y != 0) {
      matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(y));
    }

    // render the actual item
    MinecraftClient.getInstance().getItemRenderer().renderItem(item, Mode.NONE, light, OverlayTexture.DEFAULT_UV, matrices, buffer);
    matrices.pop();
  }

  /* Tooltips */
  private static final int BACKGROUND_COLOR = 0xF0100010;
  private static final int START_COLOR = 0x505000FF;
  private static final int END_COLOR = 0x5028007F;
  private static final int Z_INDEX = 400;

  /**
   * Draws hovering text on the screen
   * @param mStack         Matrix stack instance
   * @param textLines      Tooltip lines
   * @param mouseX         Mouse X position
   * @param mouseY         Mouse Y position
   * @param screenWidth    Screen width
   * @param screenHeight   Screen height
   * @param maxTextWidth   Max text width
   * @param font           Font
   * @deprecated   Remove when {@link GuiUtils} updates drawHoveringText
   */
  @Deprecated
  public static void drawHoveringText(MatrixStack mStack, List<Text> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, TextRenderer font) {
    drawHoveringText(mStack, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth,
                     GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, GuiUtils.DEFAULT_BORDER_COLOR_END, font);
  }

  /**
   * @deprecated remove with {@link #drawHoveringText(MatrixStack, List, int, int, int, int, int, TextRenderer)}
   */
  @Deprecated
  private static void drawHoveringText(MatrixStack mStack, List<Text> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth,
                                       int backgroundColor, int borderColorStart, int borderColorEnd, TextRenderer font) {
    if (textLines.isEmpty()) {
      return;
    }

    // get largest width
    int tooltipWidth = 0;
    for(StringVisitable processor : textLines) {
      int width = font.getWidth(processor);
      if (width > tooltipWidth) {
        tooltipWidth = width;
      }
    }

    // determine if we need to wrap the tooltip
    boolean needsWrap = false;
    int titleLinesCount = 1;
    int tooltipX = mouseX + 12;
    if (tooltipX + tooltipWidth + 4 > screenWidth) {
      tooltipX = mouseX - 16 - tooltipWidth;
      // if the tooltip doesn't fit on the screen
      if (tooltipX < 4) {
        if (mouseX > screenWidth / 2)
          tooltipWidth = mouseX - 12 - 8;
        else
          tooltipWidth = screenWidth - 16 - mouseX;
        needsWrap = true;
      }
    }
    if (maxTextWidth > 0 && tooltipWidth > maxTextWidth) {
      tooltipWidth = maxTextWidth;
      needsWrap = true;
    }

    // uf wrap is needed, wrap text
    List<OrderedText> finalText;
    if (needsWrap) {
      int wrappedTooltipWidth = 0;
      List<OrderedText> wrappedTextLines = new ArrayList<>();
      for (int i = 0; i < textLines.size(); i++) {
        StringVisitable textLine = textLines.get(i);
        List<OrderedText> wrappedLine = font.wrapLines(textLine, tooltipWidth);
        if (i == 0)
          titleLinesCount = wrappedLine.size();
        for (OrderedText line : wrappedLine) {
          int lineWidth = font.getWidth(line);
          if (lineWidth > wrappedTooltipWidth)
            wrappedTooltipWidth = lineWidth;
          wrappedTextLines.add(line);
        }
      }
      tooltipWidth = wrappedTooltipWidth;
      finalText = wrappedTextLines;
      if (mouseX > screenWidth / 2)
        tooltipX = mouseX - 16 - tooltipWidth;
      else
        tooltipX = mouseX + 12;
    } else {
      finalText = Lists.transform(textLines, Text::asOrderedText);
    }

    int tooltipY = mouseY - 12;
    int tooltipHeight = 8;
    if (textLines.size() > 1) {
      tooltipHeight += (textLines.size() - 1) * 10;
      // gap between title lines and next lines
      if (textLines.size() > titleLinesCount)
        tooltipHeight += 2;
    }
    if (tooltipY < 4)
      tooltipY = 4;
    else if (tooltipY + tooltipHeight + 4 > screenHeight)
      tooltipY = screenHeight - tooltipHeight - 4;

    mStack.push();

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    buffer.begin(7, VertexFormats.POSITION_COLOR);
    Matrix4f mat = mStack.peek().getModel();
    fillGradient(mat, buffer, tooltipX - 3,                tooltipY - 4,                 tooltipX + tooltipWidth + 3, tooltipY - 3,                     400, backgroundColor,  backgroundColor);
    fillGradient(mat, buffer, tooltipX - 3,                tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4,     400, backgroundColor,  backgroundColor);
    fillGradient(mat, buffer, tooltipX - 3,                tooltipY - 3,                 tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3,     400, backgroundColor,  backgroundColor);
    fillGradient(mat, buffer, tooltipX - 4,                tooltipY - 3,                 tooltipX - 3,                tooltipY + tooltipHeight + 3,     400, backgroundColor,  backgroundColor);
    fillGradient(mat, buffer, tooltipX + tooltipWidth + 3, tooltipY - 3,                 tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3,     400, backgroundColor,  backgroundColor);
    fillGradient(mat, buffer, tooltipX - 3,                tooltipY - 3 + 1,             tooltipX - 3 + 1,            tooltipY + tooltipHeight + 3 - 1, 400, borderColorStart, borderColorEnd);
    fillGradient(mat, buffer, tooltipX + tooltipWidth + 2, tooltipY - 3 + 1,             tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3 - 1, 400, borderColorStart, borderColorEnd);
    fillGradient(mat, buffer, tooltipX - 3,                tooltipY - 3,                 tooltipX + tooltipWidth + 3, tooltipY - 3 + 1,                 400, borderColorStart, borderColorStart);
    fillGradient(mat, buffer, tooltipX - 3,                tooltipY + tooltipHeight + 2, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 3,     400, borderColorEnd,   borderColorEnd);
    RenderSystem.enableDepthTest();
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();
    RenderSystem.shadeModel(7425);
    buffer.end();
    BufferRenderer.draw(buffer);
    RenderSystem.shadeModel(7424);
    RenderSystem.disableBlend();
    RenderSystem.enableTexture();
    VertexConsumerProvider.Immediate renderType = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
    mStack.translate(0.0D, 0.0D, 400.0D);

    for(int line = 0; line < finalText.size(); ++line) {
      OrderedText processor = finalText.get(line);
      if (processor != null) {
        font.draw(processor, tooltipX, tooltipY, -1, true, mat, renderType, false, 0, 15728880);
      }
      if (line == 0) {
        tooltipY += 2;
      }

      tooltipY += 10;
    }

    renderType.draw();
    mStack.pop();
  }

  /**
   * Fills a gradient with color
   * @param matrix   Transforms
   * @param builder  Buffer builder instance
   * @param x1       Start X
   * @param y1       Start Y
   * @param x2       End X
   * @param y2       End Y
   * @param z        Z index
   * @param colorA   Start color
   * @param colorB   End color
   * @deprecated remove with {@link #drawHoveringText(MatrixStack, List, int, int, int, int, int, TextRenderer)}
   */
  @Deprecated
  private static void fillGradient(Matrix4f matrix, BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int colorA, int colorB) {
    float alphaA = (float)(colorA >> 24 & 255) / 255F;
    float redA   = (float)(colorA >> 16 & 255) / 255F;
    float greenA = (float)(colorA >>  8 & 255) / 255F;
    float blueA  = (float)(colorA       & 255) / 255F;
    float alphaB = (float)(colorB >> 24 & 255) / 255F;
    float redB   = (float)(colorB >> 16 & 255) / 255F;
    float greenB = (float)(colorB >>  8 & 255) / 255F;
    float blueB  = (float)(colorB       & 255) / 255F;
    builder.vertex(matrix, (float)x2, (float)y1, (float)z).color(redA, greenA, blueA, alphaA).next();
    builder.vertex(matrix, (float)x1, (float)y1, (float)z).color(redA, greenA, blueA, alphaA).next();
    builder.vertex(matrix, (float)x1, (float)y2, (float)z).color(redB, greenB, blueB, alphaB).next();
    builder.vertex(matrix, (float)x2, (float)y2, (float)z).color(redB, greenB, blueB, alphaB).next();
  }
}

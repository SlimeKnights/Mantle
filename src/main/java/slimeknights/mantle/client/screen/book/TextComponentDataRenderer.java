package slimeknights.mantle.client.screen.book;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import slimeknights.mantle.client.book.data.element.TextComponentData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextComponentDataRenderer {

  /**
   * Renders the given Text Components on the screen and returns the action if any of them have one.
   *
   * @param graphics the matrix stack to render with
   * @param x           the x position to render at
   * @param y           the y position to render at
   * @param boxWidth    the width of the given render box
   * @param boxHeight   the height of the given render box
   * @param data        the list of text component data to draw
   * @param mouseX      the mouseY
   * @param mouseY      the mouseX
   * @param fr          the font renderer
   * @param tooltip     the list of tooltips
   * @return the action if there's any
   */
  public static String drawText(GuiGraphics graphics, int x, int y, int boxWidth, int boxHeight, TextComponentData[] data, int mouseX, int mouseY, Font fr, List<Component> tooltip) {
    String action = "";

    int atX = x;
    int atY = y;

    float prevScale = 1.F;

    for (TextComponentData item : data) {
      int box1X, box1Y, box1W = 9999, box1H = y + fr.lineHeight;
      int box2X, box2Y = 9999, box2W, box2H;
      int box3X = 9999, box3Y = 9999, box3W, box3H;

      if (item == null || item.text == null) {
        continue;
      }

      if (item.text.getString().equals("\n")) {
        atX = x;
        atY += fr.lineHeight;
        continue;
      }

      if (item.isParagraph) {
        atX = x;
        atY += fr.lineHeight * 2 * prevScale;
      }

      prevScale = item.scale;

      List<FormattedText> textLines = splitTextComponentBySize(item.text, boxWidth, boxHeight - (atY - y), boxWidth - (atX - x), fr, item.scale);

      box1X = atX;
      box1Y = atY;
      box2X = x;
      box2W = x + boxWidth;

      for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
        if (lineNumber == textLines.size() - 1) {
          box3X = atX;
          box3Y = atY;
        }

        FormattedText textComponent = textLines.get(lineNumber);
        drawScaledTextComponent(graphics, fr, textComponent, atX, atY, item.dropShadow, item.scale);

        if (lineNumber < textLines.size() - 1) {
          atY += fr.lineHeight;
          atX = x;
        }

        if (lineNumber == 0) {
          box2Y = atY;

          if (atX == x) {
            box1W = x + boxWidth;
          } else {
            box1W = atX;
          }
        }
      }

      box2H = atY;

      atX += fr.width(Language.getInstance().getVisualOrder(textLines.get(textLines.size() - 1))) * item.scale;
      if (atX - x >= boxWidth) {
        atX = x;
        atY += fr.lineHeight * item.scale;
      }

      box3W = atX;
      box3H = (int) (atY + fr.lineHeight * item.scale);

      boolean mouseCheck = (mouseX >= box1X && mouseX <= box1W && mouseY >= box1Y && mouseY <= box1H && box1X != box1W && box1Y != box1H) || (mouseX >= box2X && mouseX <= box2W && mouseY >= box2Y && mouseY <= box2H && box2X != box2W && box2Y != box2H) || (mouseX >= box3X && mouseX <= box3W && mouseY >= box3Y && mouseY <= box3H && box3X != box3W && box1Y != box3H);

      if (item.tooltips != null && item.tooltips.length > 0) {
        // render bounding boxes for event handling
        if (BookScreen.debug) {
          graphics.fillGradient(box1X,  box1Y,  box1W,      box1H,      0xFF00FF00, 0xFF00FF00);
          graphics.fillGradient(box2X,  box2Y,  box2W,      box2H,      0xFFFF0000, 0xFFFF0000);
          graphics.fillGradient(box3X,  box3Y,  box3W,      box3H,      0xFF0000FF, 0xFF0000FF);
          graphics.fillGradient(mouseX, mouseY, mouseX + 5, mouseY + 5, 0xFFFF00FF, 0xFFFFFF00);
        }

        if (mouseCheck) {
          tooltip.addAll(Arrays.asList(item.tooltips));
        }
      }

      if (item.action != null && !item.action.isEmpty()) {
        if (mouseCheck) {
          action = item.action;
        }
      }

      if (atY >= y + boxHeight) {
        graphics.drawString(fr, "...", atX, atY, 0, item.dropShadow);
        break;
      }

      y = atY;
    }

    if (BookScreen.debug && !action.isEmpty()) {
      tooltip.add(Component.empty());
      tooltip.add(Component.literal("Action: " + action).withStyle(ChatFormatting.GRAY));
    }

    return action;
  }

  /**
   * @param textComponent the actual text component to split
   * @param width         the width of the text
   * @param height        the height of the text
   * @param firstWidth    the first with of the text
   * @param fontRenderer  the font renderer to use
   * @param scale         the scale to use
   * @return the list of split text components based on the given size
   */
  public static List<FormattedText> splitTextComponentBySize(Component textComponent, int width, int height, int firstWidth, Font fontRenderer, float scale) {
    int curWidth = (int) (fontRenderer.width(textComponent) * scale);

    int curHeight = (int) (fontRenderer.lineHeight * scale);
    boolean needsWrap = false;
    List<FormattedText> textLines = new ArrayList<>();

    if ((curHeight == (int) (fontRenderer.lineHeight * scale) && curWidth > firstWidth) || (curHeight != (int) (fontRenderer.lineHeight * scale) && curWidth > width)) {
      needsWrap = true;
    }

    if (needsWrap) {
      textLines = new ArrayList<>(fontRenderer.getSplitter().splitLines(textComponent, firstWidth, Style.EMPTY));
    } else {
      textLines.add(textComponent);
    }

    return textLines;
  }

  /**
   * Draws a text component with the given scale at the given position
   *
   * @param graphics      the given graphics used for rendering.
   * @param font          the font renderer to render with
   * @param textComponent the text component to render
   * @param x             the x position to render at
   * @param y             the y position to render at
   * @param dropShadow    if there should be a shadow on the text
   * @param scale         the scale to render as
   */
  public static void drawScaledTextComponent(GuiGraphics graphics, Font font, FormattedText textComponent, float x, float y, boolean dropShadow, float scale) {
    PoseStack poseStack = graphics.pose();
    poseStack.pushPose();
    poseStack.translate(x, y, 0);
    poseStack.scale(scale, scale, 1F);

    graphics.drawString(font, Language.getInstance().getVisualOrder(textComponent), 0, 0, 0, dropShadow);
    poseStack.popPose();
  }
}

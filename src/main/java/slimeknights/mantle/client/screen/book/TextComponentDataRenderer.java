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
  private static final float DEFAULT_TEXT_SCALE = 0.82f;
  private static final float MIN_AUTO_SCALE = 0.75f;

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
  // TODO: can we merge this with TextDataRenderer, put the differences in TextData vs TextComponentData?
  public static String drawText(GuiGraphics graphics, int x, int y, int boxWidth, int boxHeight, TextComponentData[] data, int mouseX, int mouseY, Font fr, List<Component> tooltip) {
    String action = "";

    int atX = x;
    int atY = y;
    int startY = y;

    float prevScale = 1.F;

    for (TextComponentData item : data) {
      int box1X, box1Y, box1W = 9999, box1H = y + fr.lineHeight;
      int box2X, box2Y = 9999, box2W, box2H;
      int box3X = 9999, box3Y = 9999, box3W, box3H;

      if (item == null) {
        continue;
      }
      float itemScale = defaultScale(item.scale);
      // allow specifying linebreak on its own to force a linebreak
      if (item.text == null) {
        if (item.linebreak) {
          atX = x;
          atY += scaledLineHeight(fr, itemScale);
        }
        continue;
      }

      // TODO: ditch this, the linebreak field handles it so much better
      if (item.text.getString().equals("\n")) {
        atX = x;
        atY += scaledLineHeight(fr, itemScale);
        continue;
      }

      if (item.isParagraph) {
        atX = x;
        atY += fr.lineHeight * 2 * prevScale;
      }

      prevScale = itemScale;

      int remainingHeight = Math.max(1, boxHeight - (atY - startY));

      List<FormattedText> textLines;
      int firstWidth = boxWidth - (atX - x);
      if (item.linebreak) {
        textLines = List.of(item.text);
      } else {
        textLines = splitTextComponentBySize(item.text, boxWidth, Short.MAX_VALUE, firstWidth, fr, itemScale);
      }
      if (textLines.isEmpty()) {
        break;
      }

      float blockScale = fitScaleToHeight(textLines.size(), remainingHeight, fr, itemScale);
      box1X = atX;
      box1Y = atY;
      box2X = x;
      box2W = x + boxWidth;

      int drawnLines = 0;
      FormattedText lastDrawnLine = FormattedText.EMPTY;
      float lastDrawnScale = blockScale;
      for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
        FormattedText textComponent = textLines.get(lineNumber);
        int lineWidth = lineNumber == 0 ? boxWidth - (atX - x) : boxWidth;
        float lineScale = fitScaleToWidth(textComponent, lineWidth, fr, blockScale);
        int lineHeight = scaledLineHeight(fr, lineScale);
        if (lineNumber == textLines.size() - 1) {
          box3X = atX;
          box3Y = atY;
        }

        drawScaledTextComponent(graphics, fr, textComponent, atX, atY, item.dropShadow, lineScale);
        drawnLines++;
        lastDrawnLine = textComponent;
        lastDrawnScale = lineScale;

        if (lineNumber < textLines.size() - 1) {
          atY += lineHeight;
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
      if (drawnLines == 0) {
        break;
      }

      box2H = atY;

      atX += fr.width(Language.getInstance().getVisualOrder(lastDrawnLine)) * lastDrawnScale;
      // if specified, include a trailing linebreak, works better than a separate linebreak element on handling whitespace
      if (item.linebreak || atX - x >= boxWidth) {
        atX = x;
        atY += scaledLineHeight(fr, lastDrawnScale);
      }

      box3W = atX;
      box3H = atY + scaledLineHeight(fr, lastDrawnScale);

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
    if (textComponent == null || width <= 0 || height <= 0 || firstWidth <= 0 || scale <= 0) {
      return List.of();
    }

    int lineHeight = Math.max(1, (int)(fontRenderer.lineHeight * scale));
    int maxLines = height / lineHeight;
    if (maxLines <= 0) {
      return List.of();
    }

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

    if (textLines.size() > maxLines) {
      float fittedScale = fitScaleToHeight(textLines.size(), height, fontRenderer, scale);
      if (fittedScale < scale) {
        textLines = new ArrayList<>(fontRenderer.getSplitter().splitLines(textComponent, (int)(width / fittedScale), Style.EMPTY));
      }
    }

    return textLines;
  }

  private static int scaledLineHeight(Font fontRenderer, float scale) {
    return Math.max(1, (int)(fontRenderer.lineHeight * scale));
  }

  private static float defaultScale(float scale) {
    return scale >= 1 ? scale * DEFAULT_TEXT_SCALE : scale;
  }

  private static float fitScaleToHeight(int lines, int height, Font fontRenderer, float scale) {
    if (lines <= 0 || height <= 0 || scale <= 0) {
      return scale;
    }
    float neededHeight = fontRenderer.lineHeight * scale * lines;
    if (neededHeight <= height) {
      return scale;
    }
    return Math.max(MIN_AUTO_SCALE, height / (float)(fontRenderer.lineHeight * lines));
  }

  private static float fitScaleToWidth(FormattedText textComponent, int width, Font fontRenderer, float scale) {
    if (textComponent == null || width <= 0 || scale <= 0) {
      return scale;
    }
    int textWidth = fontRenderer.width(textComponent);
    if (textWidth <= 0 || textWidth * scale <= width) {
      return scale;
    }
    return Math.max(MIN_AUTO_SCALE, width / (float)textWidth);
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

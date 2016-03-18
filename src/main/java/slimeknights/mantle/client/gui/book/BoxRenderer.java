package slimeknights.mantle.client.gui.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.MathHelper;
import static slimeknights.mantle.client.gui.book.Textures.TEX_BOX;

public class BoxRenderer {

  public static final int TEX_SIZE = 128;

  public static void drawBox(int x, int y, int width, int height, int index) {
    drawBox(x, y, width, height, index, 16, 16, 4, 4, 2, 2);
  }

  public static void drawBox(int x, int y, int width, int height, int index, int sprW, int sprH, int segW, int segH, int segGW, int segGH) {
    Minecraft.getMinecraft().renderEngine.bindTexture(TEX_BOX);

    int sx = x, sy = y;

    for (int i = 0; i < 3; i++) {
      int sgW = width - segW * 2;

      if (i % 2 == 0)
        sgW = segW;

      for (int j = 0; j < 3; j++) {
        int sgH = height - segH * 2;

        if (j % 2 == 0)
          sgH = segH;

        int[] matrix = getTextureMatrixCoords(index, sprW, sprH, segW, segH, segGW, segGH, i + j * 3);
        Gui.drawScaledCustomSizeModalRect(sx, sy, matrix[0], matrix[1], segW, segH, sgW, sgH, TEX_SIZE, TEX_SIZE);

        sy += sgH;
      }

      sx += sgW;
      sy = y;
    }
  }

  public static int[] getTextureMatrixCoords(int index, int sprW, int sprH, int segW, int segH, int segGW, int segGH, int segId) {
    int totalWidth = segW * 3 + segGW * 2;
    int totalHeight = segH * 3 + segGH * 2;

    return getTextureMatrixCoords((index % (256 / sprW) * sprW) + (sprW / 2 - totalWidth / 2), (MathHelper.floor_float(index / (256F / sprH)) * sprH) + (sprH / 2 - totalHeight / 2), segW, segH, segGW, segGH, segId);
  }

  public static int[] getTextureMatrixCoords(int u, int v, int segW, int segH, int segGW, int segGH, int segId) {
    int[] coords = new int[4];

    int col = segId % 3;
    int row = MathHelper.floor_float(segId / 3F);

    coords[0] = u + col * segW + col * segGW;
    coords[1] = v + row * segH + row * segGH;
    coords[2] = coords[0] + segW;
    coords[3] = coords[1] + segH;

    return coords;
  }
}

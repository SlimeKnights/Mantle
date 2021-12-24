package slimeknights.mantle.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import slimeknights.mantle.client.model.inventory.ModelItem;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class RenderingHelper {
  /* Rotation */

  /**
   * Applies horizontal rotation to the given TESR
   * @param matrices  Matrix stack
   * @param state     Block state, checked for {@link BlockStateProperties#HORIZONTAL_FACING}
   * @return  True if rotation was applied. Caller is expected to call {@link MatrixStack#pop()} if true
   */
  public static boolean applyRotation(MatrixStack matrices, BlockState state) {
    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
      return applyRotation(matrices, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
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
      matrices.pushPose();
      matrices.translate(0.5, 0, 0.5);
      matrices.mulPose(Vector3f.YP.rotationDegrees(-90f * (facing.get2DDataValue())));
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
  public static void renderItem(MatrixStack matrices, IRenderTypeBuffer buffer, ItemStack item, ModelItem modelItem, int light) {
    // if the item says skip, skip
    if (modelItem.isHidden()) return;
    // if no stack, skip
    if (item.isEmpty()) return;

    // start rendering
    matrices.pushPose();
    Vector3f center = modelItem.getCenterScaled();
    matrices.translate(center.x(), center.y(), center.z());

    // scale
    float scale = modelItem.getSizeScaled();
    matrices.scale(scale, scale, scale);

    // rotate X, then Y
    float x = modelItem.getX();
    if (x != 0) {
      matrices.mulPose(Vector3f.XP.rotationDegrees(x));
    }
    float y = modelItem.getY();
    if (y != 0) {
      matrices.mulPose(Vector3f.YP.rotationDegrees(y));
    }

    // render the actual item
    Minecraft.getInstance().getItemRenderer().renderStatic(item, modelItem.getTransform(), light, OverlayTexture.NO_OVERLAY, matrices, buffer);
    matrices.popPose();
  }

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
   * @deprecated   Remove in 1.17, use {@link GuiUtils#drawHoveringText(MatrixStack, List, int, int, int, int, int, FontRenderer)}
   */
  @Deprecated
  public static void drawHoveringText(MatrixStack mStack, List<ITextComponent> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font) {
    GuiUtils.drawHoveringText(mStack, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, GuiUtils.DEFAULT_BACKGROUND_COLOR, GuiUtils.DEFAULT_BORDER_COLOR_START, GuiUtils.DEFAULT_BORDER_COLOR_END, font);
  }

  /**
   * @deprecated   Remove in 1.17, use {@link GuiUtils#drawHoveringText(MatrixStack, List, int, int, int, int, int, int, int, int, FontRenderer)}
   */
  @Deprecated
  private static void drawHoveringText(MatrixStack mStack, List<ITextComponent> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth,
                                       int backgroundColor, int borderColorStart, int borderColorEnd, FontRenderer font) {
    GuiUtils.drawHoveringText(mStack, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, backgroundColor, borderColorStart, borderColorEnd, font);
  }
}

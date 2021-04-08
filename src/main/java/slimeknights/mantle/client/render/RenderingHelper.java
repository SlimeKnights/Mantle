package slimeknights.mantle.client.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
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
   * @param state     Block state, checked for {@link BlockStateProperties#HORIZONTAL_FACING}
   * @return  True if rotation was applied. Caller is expected to call {@link MatrixStack#pop()} if true
   */
  public static boolean applyRotation(MatrixStack matrices, BlockState state) {
    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
      return applyRotation(matrices, state.get(BlockStateProperties.HORIZONTAL_FACING));
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
      matrices.rotate(Vector3f.YP.rotationDegrees(-90f * (facing.getHorizontalIndex())));
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
    matrices.push();
    Vector3f center = modelItem.getCenterScaled();
    matrices.translate(center.getX(), center.getY(), center.getZ());

    // scale
    float scale = modelItem.getSizeScaled();
    matrices.scale(scale, scale, scale);

    // rotate X, then Y
    float x = modelItem.getX();
    if (x != 0) {
      matrices.rotate(Vector3f.XP.rotationDegrees(x));
    }
    float y = modelItem.getY();
    if (y != 0) {
      matrices.rotate(Vector3f.YP.rotationDegrees(y));
    }

    // render the actual item
    Minecraft.getInstance().getItemRenderer().renderItem(item, TransformType.NONE, light, OverlayTexture.NO_OVERLAY, matrices, buffer);
    matrices.pop();
  }

  /* Tooltips */
  private static final int BACKGROUND_COLOR = 0xF0100010;
  private static final int START_COLOR = 0x505000FF;
  private static final int END_COLOR = 0x5028007F;
  private static final int Z_INDEX = 400;
}

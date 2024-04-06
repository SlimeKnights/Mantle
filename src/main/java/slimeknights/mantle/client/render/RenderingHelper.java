package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3f;
import slimeknights.mantle.client.model.inventory.ModelItem;

@SuppressWarnings("WeakerAccess")
public class RenderingHelper {
  /* Rotation */

  /**
   * Applies horizontal rotation to the given TESR
   * @param matrices  Matrix stack
   * @param state     Block state, checked for {@link BlockStateProperties#HORIZONTAL_FACING}
   * @return  True if rotation was applied. Caller is expected to call {@link PoseStack#popPose()} if true
   */
  public static boolean applyRotation(PoseStack matrices, BlockState state) {
    if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
      return applyRotation(matrices, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
    return false;
  }

  /**
   * Applies horizontal rotation to the given TESR
   * @param matrices  Matrix stack
   * @param facing    Direction of rotation
   * @return  True if rotation was applied. Caller is expected to call {@link PoseStack#popPose()} if true
   */
  public static boolean applyRotation(PoseStack matrices, Direction facing) {
    // south has a facing of 0, no rotation needed
    if (facing.getAxis().isHorizontal() && facing != Direction.SOUTH) {
      matrices.pushPose();
      matrices.translate(0.5, 0, 0.5);
      matrices.mulPose(Axis.YP.rotationDegrees(-90f * (facing.get2DDataValue())));
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
  public static void renderItem(PoseStack matrices, MultiBufferSource buffer, ItemStack item, ModelItem modelItem, int light) {
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
      matrices.mulPose(Axis.XP.rotationDegrees(x));
    }
    float y = modelItem.getY();
    if (y != 0) {
      matrices.mulPose(Axis.YP.rotationDegrees(y));
    }

    // render the actual item
    Minecraft.getInstance().getItemRenderer().renderStatic(item, modelItem.getTransform(), light, OverlayTexture.NO_OVERLAY, matrices, buffer, Minecraft.getInstance().level, 0);
    matrices.popPose();
  }
}

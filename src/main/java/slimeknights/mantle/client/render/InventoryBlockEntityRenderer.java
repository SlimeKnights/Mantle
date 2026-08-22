package slimeknights.mantle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class InventoryBlockEntityRenderer<T extends BlockEntity & Container> implements BlockEntityRenderer<T> {
  public InventoryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

  @Override
  public void render(T inventory, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int light, int combinedOverlayIn) {
    if (inventory.isEmpty()) return;

    // first, find the model for item display locations
    BlockState state = inventory.getBlockState();
    List<RenderItem> renderItems = RenderItem.REGISTRY.get(state, List.of());
    if (!renderItems.isEmpty()) {
      // if the block is rotatable, rotate item display
      boolean isRotated = RenderingHelper.applyRotation(matrices, state);

      // render items
      for (int i = 0; i < renderItems.size(); i++) {
        RenderingHelper.renderItem(matrices, buffer, inventory.getItem(i), renderItems.get(i), light);
      }

      // pop back rotation
      if (isRotated) {
        matrices.popPose();
      }
    }
  }

  @Override
  public boolean shouldRenderOffScreen(T tile) {
    return !tile.isEmpty();
  }
}

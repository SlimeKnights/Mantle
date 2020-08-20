package slimeknights.mantle.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import slimeknights.mantle.client.model.inventory.InventoryModel;
import slimeknights.mantle.client.model.inventory.ModelItem;
import slimeknights.mantle.client.model.util.ModelHelper;

import java.util.List;

public class InventoryTileEntityRenderer<T extends TileEntity & IInventory> extends TileEntityRenderer<T> {

  public InventoryTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }

  @Override
  public void render(T inventory, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int light, int combinedOverlayIn) {
    if (inventory.isEmpty()) return;

    // first, find the model for item display locations
    BlockState state = inventory.getBlockState();
    InventoryModel.BakedModel model = ModelHelper.getBakedModel(state, InventoryModel.BakedModel.class);
    if (model != null) {
      // if the block is rotatable, rotate item display
      boolean isRotated = RenderingHelper.applyRotation(matrices, state);

      // render items
      List<ModelItem> modelItems = model.getItems();
      for (int i = 0; i < modelItems.size(); i++) {
        RenderingHelper.renderItem(matrices, buffer, inventory.getStackInSlot(i), modelItems.get(i), light);
      }

      // pop back rotation
      if (isRotated) {
        matrices.pop();
      }
    }
  }
}

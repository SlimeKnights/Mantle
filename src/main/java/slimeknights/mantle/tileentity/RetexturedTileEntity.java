package slimeknights.mantle.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.LazyValue;
import net.minecraftforge.client.model.data.IModelData;
import slimeknights.mantle.util.RetexturedHelper;

/**
 * Minimal implementation for {@link IRetexturedTileEntity}, use alongside {@link slimeknights.mantle.block.RetexturedBlock} and {@link slimeknights.mantle.item.RetexturedBlockItem}
 */
public class RetexturedTileEntity extends MantleTileEntity implements IRetexturedTileEntity {

  /** Lazy value of model data as it will not change after first fetch */
  private final LazyValue<IModelData> data = new LazyValue<>(this::getRetexturedModelData);
  public RetexturedTileEntity(TileEntityType<?> type) {
    super(type);
  }

  @Override
  public IModelData getModelData() {
    return data.getValue();
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void read(BlockState blockState, CompoundNBT tags) {
    String oldName = getTextureName();
    super.read(blockState, tags);
    String newName = getTextureName();
    // if the texture name changed, mark the position for rerender
    if (!oldName.equals(newName) && world != null && world.isRemote) {
      data.getValue().setData(RetexturedHelper.BLOCK_PROPERTY, getTexture());
      requestModelDataUpdate();
      world.notifyBlockUpdate(pos, blockState, blockState, 0);
    }
  }
}

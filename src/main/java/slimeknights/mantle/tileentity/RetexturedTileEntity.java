package slimeknights.mantle.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.LazyValue;
import net.minecraftforge.client.model.data.IModelData;

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
  public CompoundNBT getUpdateTag() {
    // already included in regular NBT as this lives in ForgeData, just copy to the update tag
    CompoundNBT nbt = super.getUpdateTag();
    writeTextureToUpdateTag(nbt);
    return nbt;
  }
}

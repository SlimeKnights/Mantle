package slimeknights.mantle.tileentity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Lazy;
import net.minecraftforge.client.model.data.IModelData;

/**
 * Minimal implementation for {@link IRetexturedTileEntity}, use alongside {@link slimeknights.mantle.block.RetexturedBlock} and {@link slimeknights.mantle.item.RetexturedBlockItem}
 */
public class RetexturedTileEntity extends BlockEntity implements IRetexturedTileEntity {

  /** Lazy value of model data as it will not change after first fetch */
  private final Lazy<IModelData> data = new Lazy<>(this::getRetexturedModelData);
  public RetexturedTileEntity(BlockEntityType<?> type) {
    super(type);
  }

  @Override
  public IModelData getModelData() {
    return data.get();
  }

  @Override
  public CompoundTag toInitialChunkDataTag() {
    // new tag instead of super since default implementation calls the super of writeToNBT
    return toTag(new CompoundTag());
  }
}

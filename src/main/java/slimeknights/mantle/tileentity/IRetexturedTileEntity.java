package slimeknights.mantle.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import slimeknights.mantle.util.RetexturedHelper;

/**
 * Standard interface that should be used by retexturable tile entities, allows control over where the texture is saved.
 *
 * Use alongside {@link slimeknights.mantle.block.RetexturedBlock} and {@link slimeknights.mantle.item.RetexturedBlockItem}. See {@link RetexturedTileEntity} for implementation.
 */
public interface IRetexturedTileEntity {
  /* Gets the Forge tile data for the tile entity */
  CompoundNBT getTileData();

  /**
   * Gets the current texture block name
   * @return Texture block name
   */
  default String getTextureName() {
    return RetexturedHelper.getTextureName(getTileData());
  }
  /**
   * Gets the current texture block
   * @return Texture block
   */
  default Block getTexture() {
    return RetexturedHelper.getBlock(getTextureName());
  }

  /**
   * Updates the texture to the given name
   * @param name  Texture name
   */
  default void updateTexture(String name) {
    RetexturedHelper.setTexture(getTileData(), name);
  }

  /**
   * Gets the model data instance with the relevant texture block
   * @return  Model data for the TE
   */
  default IModelData getRetexturedModelData() {
    // texture not loaded
    ModelDataMap.Builder data = new ModelDataMap.Builder();
    Block block = getTexture();
    if (block != Blocks.AIR) {
      data = data.withInitial(RetexturedHelper.BLOCK_PROPERTY, block);
    }
    return data.build();
  }
}

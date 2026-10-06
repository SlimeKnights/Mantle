package slimeknights.mantle.block.entity;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity.DataComponentInput;
import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.mantle.registration.MantleData;
import slimeknights.mantle.util.RetexturedHelper;

/**
 * Standard interface that should be used by retexturable tile entities, allows control over where the texture is saved.
 * Note that in the future, more of these methods will be made abstract, discouraging the use of {@link #getPersistentData()} to store the texture (as we can sync our own tag easier)
 * <p>
 * Use alongside {@link RetexturedBlock} and {@link RetexturedHelper}. See {@link DefaultRetexturedBlockEntity} for implementation.
 */
public interface IRetexturedBlockEntity {
  /* Gets the Forge tile data for the tile entity */
  CompoundTag getPersistentData();

  /**
   * Gets the current texture block name. Encouraged to override this to not use {@link #getPersistentData()}
   * @return Texture block name
   */
  default String getTextureName() {
    return RetexturedHelper.getTextureName(getPersistentData());
  }

  /**
   * Gets the current texture block
   * @return Texture block
   */
  default Block getTexture() {
    return RetexturedHelper.getBlock(getTextureName());
  }

  /**
   * Updates the texture to the given name. Encouraged to override this to not use {@link #getPersistentData()}
   *
   * @param texture Texture name
   */
  default void updateTexture(Block texture) {
    String oldName = getTextureName();
    String newName = RetexturedHelper.getTextureName(texture);
    RetexturedHelper.setTexture(getPersistentData(), newName);
    if (!oldName.equals(newName)) {
      // this is an unchecked cast, but no one should be using this interface not on a block entity
      RetexturedHelper.onTextureUpdated((BlockEntity)this);
    }
  }


  /* Component copying helpers */

  /**
   * Implementation of {@link BlockEntity#applyImplicitComponents(DataComponentInput)} using {@link #updateTexture(Block)}.
   * For some implementations it may be easier to manually implement that method and bypass the block updates.
   */
  static void applyImplicitComponents(IRetexturedBlockEntity blockEntity, BlockEntity.DataComponentInput input) {
    blockEntity.updateTexture(input.getOrDefault(MantleData.BLOCK_TEXTURE, Blocks.AIR));
  }

  /** Implementation of {@link BlockEntity#collectImplicitComponents(Builder)} using {@link #getTexture()}. */
  static void collectImplicitComponents(IRetexturedBlockEntity blockEntity, DataComponentMap.Builder builder) {
    Block texture = blockEntity.getTexture();
    if (texture != Blocks.AIR) {
      builder.set(MantleData.BLOCK_TEXTURE, texture);
    }
  }

  /** Implementation of {@link BlockEntity#collectImplicitComponents(Builder)} using data in {@link BlockEntity#getPersistentData()}. If directly managing the data you can just directly remove it. */
  static void removeComponentsFromTag(CompoundTag tag) {
    if (tag.contains("NeoForgeData", Tag.TAG_COMPOUND)) {
      CompoundTag persistentData = tag.getCompound("NeoForgeData");
      persistentData.remove(RetexturedHelper.TAG_TEXTURE);
      if (persistentData.isEmpty()) {
        tag.remove("NeoForgeData");
      }
    }
  }
}

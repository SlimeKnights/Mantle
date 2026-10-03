package slimeknights.mantle.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nonnull;

import static slimeknights.mantle.util.RetexturedHelper.TAG_TEXTURE;

/**
 * Standard implementation for {@link IRetexturedBlockEntity}, use alongside {@link RetexturedBlock} and {@link RetexturedHelper}
 */
public class DefaultRetexturedBlockEntity extends MantleBlockEntity implements IRetexturedBlockEntity {
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;
  public DefaultRetexturedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Nonnull
  @Override
  public ModelData getModelData() {
    return RetexturedHelper.getModelData(texture);
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(texture);
  }

  @Override
  public void updateTexture(Block texture) {
    Block oldTexture = this.texture;
    this.texture = texture;
    if (oldTexture != this.texture) {
      setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag tags, Provider registries) {
    super.saveSynced(tags, registries);
    if (texture != Blocks.AIR) {
      tags.putString(TAG_TEXTURE, getTextureName());
    }
  }

  @Override
  public void loadAdditional(CompoundTag tags, Provider registries) {
    super.loadAdditional(tags, registries);
    if (tags.contains(TAG_TEXTURE, Tag.TAG_STRING)) {
      texture = RetexturedHelper.getBlock(tags.getString(TAG_TEXTURE));
      RetexturedHelper.onTextureUpdated(this);
    }
  }
}

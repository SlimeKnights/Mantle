package slimeknights.mantle.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Extension of tile entity to make it nameable without requiring it to implement vanilla's container interface.
 * @see net.minecraft.world.level.block.entity.BaseContainerBlockEntity
 */
@Getter
public abstract class NameableBlockEntity extends MantleBlockEntity implements INameableMenuProvider {
  private static final String TAG_CUSTOM_NAME = "CustomName";

  /** Default title for this tile entity */
  private final Component defaultName;
  /** Title set to this tile entity */
  @Nullable
  @Setter
  private Component customName;

  public NameableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component defaultTitle) {
    super(type, pos, state);
    this.defaultName = defaultTitle;
  }

  @Override
  protected void applyImplicitComponents(DataComponentInput componentInput) {
    super.applyImplicitComponents(componentInput);
    this.customName = componentInput.get(DataComponents.CUSTOM_NAME);
  }

  @Override
  protected void collectImplicitComponents(Builder components) {
    super.collectImplicitComponents(components);
    components.set(DataComponents.CUSTOM_NAME, this.customName);
  }

  @Override
  protected void loadAdditional(CompoundTag tags, Provider registries) {
    super.loadAdditional(tags, registries);
    if (tags.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING)) {
      this.customName = BlockEntity.parseCustomNameSafe(tags.getString(TAG_CUSTOM_NAME), registries);
    }
  }

  @Override
  public void saveSynced(CompoundTag tags, Provider registries) {
    super.saveSynced(tags, registries);
    if (this.customName != null) {
      tags.putString(TAG_CUSTOM_NAME, Serializer.toJson(this.customName, registries));
    }
  }
}

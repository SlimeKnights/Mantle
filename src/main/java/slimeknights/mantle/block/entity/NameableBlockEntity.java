package slimeknights.mantle.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extension of tile entity to make it namable
 */
public abstract class NameableBlockEntity extends MantleBlockEntity implements INameableMenuProvider {
	private static final String TAG_CUSTOM_NAME = "CustomName";

	/** Default title for this tile entity */
	@Getter
	private final Component defaultName;
	/** Title set to this tile entity */
	@Getter @Setter
	private Component customName;

	public NameableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component defaultTitle) {
		super(type, pos, state);
		this.defaultName = defaultTitle;
	}

	@Override
	public void load(CompoundTag tags) {
		super.load(tags);
		if (tags.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING)) {
			this.customName = Component.Serializer.fromJson(tags.getString(TAG_CUSTOM_NAME));
		}
	}

	@Override
	public void saveSynced(CompoundTag tags) {
		super.saveSynced(tags);
		if (this.hasCustomName()) {
			tags.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(this.customName));
		}
	}
}

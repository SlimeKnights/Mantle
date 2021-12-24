package slimeknights.mantle.tileentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Extension of tile entity to make it namable
 */
public abstract class NamableTileEntity extends MantleTileEntity implements IRenamableContainerProvider {
	private static final String TAG_CUSTOM_NAME = "CustomName";

	/** Default title for this tile entity */
	@Getter
	private final Component defaultName;
	/** Title set to this tile entity */
	@Getter @Setter
	private Component customName;

	public NamableTileEntity(BlockEntityType<?> tileEntityTypeIn, Component defaultTitle) {
		super(tileEntityTypeIn);
		this.defaultName = defaultTitle;
	}

	@Override
	public void load(BlockState blockState, CompoundTag tags) {
		super.load(blockState, tags);
		if (tags.contains(TAG_CUSTOM_NAME, NBT.TAG_STRING)) {
			this.customName = Component.Serializer.fromJson(tags.getString(TAG_CUSTOM_NAME));
		}
	}

	@Override
	public void writeSynced(CompoundTag tags) {
		super.writeSynced(tags);
		if (this.hasCustomName()) {
			tags.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(this.customName));
		}
	}
}

package slimeknights.mantle.tileentity;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of tile entity to make it namable
 */
public abstract class NamableTileEntity extends MantleTileEntity implements IRenamableContainerProvider {
	private static final String TAG_CUSTOM_NAME = "CustomName";

	/** Default title for this tile entity */
	private final Text defaultName;
	/** Title set to this tile entity */
	private Text customName;

	public NamableTileEntity(BlockEntityType<?> tileEntityTypeIn, Text defaultTitle) {
		super(tileEntityTypeIn);
		this.defaultName = defaultTitle;
	}

	@Override
	public void fromTag(BlockState blockState, CompoundTag tags) {
		super.fromTag(blockState, tags);
		if (tags.contains(TAG_CUSTOM_NAME, NbtType.STRING)) {
			this.customName = Text.Serializer.fromJson(tags.getString(TAG_CUSTOM_NAME));
		}
	}

	@Override
	public void writeSynced(CompoundTag tags) {
		super.writeSynced(tags);
		if (this.hasCustomName()) {
			tags.putString(TAG_CUSTOM_NAME, Text.Serializer.toJson(this.customName));
		}
	}

	@Override
	public Text getDefaultName() {
		return defaultName;
	}

	@Nullable
	@Override
	public Text getCustomName() {
		return customName;
	}

	@Override
	public void setCustomName(Text customName) {
		this.customName = customName;
	}
}

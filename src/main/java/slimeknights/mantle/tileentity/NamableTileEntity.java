package slimeknights.mantle.tileentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Extension of tile entity to make it namable
 */
public abstract class NamableTileEntity extends MantleTileEntity implements IRenamableContainerProvider {
	private static final String TAG_CUSTOM_NAME = "CustomName";

	/** Default title for this tile entity */
	@Getter
	private final ITextComponent defaultName;
	/** Title set to this tile entity */
	@Getter @Setter
	private ITextComponent customName;

	public NamableTileEntity(TileEntityType<?> tileEntityTypeIn, ITextComponent defaultTitle) {
		super(tileEntityTypeIn);
		this.defaultName = defaultTitle;
	}

	@Override
	public void read(BlockState blockState, CompoundNBT tags) {
		super.read(blockState, tags);
		if (tags.contains(TAG_CUSTOM_NAME, NBT.TAG_STRING)) {
			this.customName = ITextComponent.Serializer.getComponentFromJson(tags.getString(TAG_CUSTOM_NAME));
		}
	}

	@Override
	public void writeSynced(CompoundNBT tags) {
		super.writeSynced(tags);
		if (this.hasCustomName()) {
			tags.putString(TAG_CUSTOM_NAME, ITextComponent.Serializer.toJson(this.customName));
		}
	}
}

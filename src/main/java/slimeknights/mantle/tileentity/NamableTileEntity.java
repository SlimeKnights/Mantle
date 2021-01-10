package slimeknights.mantle.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

/**
 * Extension of tile entity to make it namable
 */
public abstract class NamableTileEntity extends MantleTileEntity implements INamedContainerProvider, INameable {
	private static final String TAG_CUSTOM_NAME = "CustomName";

	/** Default title for this tile entity */
	private final ITextComponent defaultTitle;
	/** Title set to this tile entity */
	private ITextComponent customTitle;

	public NamableTileEntity(TileEntityType<?> tileEntityTypeIn, ITextComponent defaultTitle) {
		super(tileEntityTypeIn);
		this.defaultTitle = defaultTitle;
	}

	@Override
	public ITextComponent getName() {
		return customTitle != null ? customTitle : defaultTitle;
	}

	@Nullable
	public ITextComponent getCustomName() {
		return customTitle;
	}

	/**
	 * Sets the name for this tile entity
	 * @param customName  New custom name
	 */
	public void setCustomName(ITextComponent customName) {
		this.customTitle = customName;
	}

	@Override
	public ITextComponent getDisplayName() {
		return getName();
	}

	@Override
	public void read(BlockState blockState, CompoundNBT tags) {
		super.read(blockState, tags);
		if (tags.contains(TAG_CUSTOM_NAME, NBT.TAG_STRING)) {
			this.customTitle = ITextComponent.Serializer.getComponentFromJson(tags.getString(TAG_CUSTOM_NAME));
		}
	}

	@Override
	public void writeSynced(CompoundNBT tags) {
		super.writeSynced(tags);
		if (this.hasCustomName()) {
			tags.putString(TAG_CUSTOM_NAME, ITextComponent.Serializer.toJson(this.customTitle));
		}
	}
}

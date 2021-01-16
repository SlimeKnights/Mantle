package slimeknights.mantle.tileentity;

import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

/**
 * Interface for containers that can be renamed. Used in {@link slimeknights.mantle.block.InventoryBlock} to set the name on placement
 */
public interface IRenamableContainerProvider extends INamedContainerProvider, INameable {

	/**
	 * Gets the default name of this tile entity
	 * @return  Default name
	 */
	ITextComponent getDefaultName();

	/**
	 * Gets the custom name for this tile entity
	 * @return  Custom name
	 */
	@Override
	@Nullable
	ITextComponent getCustomName();

	/**
	 * Sets the name for this tile entity
	 * @param name  New custom name
	 */
	void setCustomName(ITextComponent name);

	@Override
	default ITextComponent getName() {
		ITextComponent customTitle = getCustomName();
		return customTitle != null ? customTitle : getDefaultName();
	}

	@Override
	default ITextComponent getDisplayName() {
		return getName();
	}
}

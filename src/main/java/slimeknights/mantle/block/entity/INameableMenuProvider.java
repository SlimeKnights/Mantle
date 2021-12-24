package slimeknights.mantle.block.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;

import javax.annotation.Nullable;

/**
 * Interface for containers that can be renamed. Used in {@link slimeknights.mantle.block.InventoryBlock} to set the name on placement
 */
public interface INameableMenuProvider extends MenuProvider, Nameable {

	/**
	 * Gets the default name of this tile entity
	 * @return  Default name
	 */
	Component getDefaultName();

	/**
	 * Gets the custom name for this tile entity
	 * @return  Custom name
	 */
	@Override
	@Nullable
	Component getCustomName();

	/**
	 * Sets the name for this tile entity
	 * @param name  New custom name
	 */
	void setCustomName(Component name);

	@Override
	default Component getName() {
		Component customTitle = getCustomName();
		return customTitle != null ? customTitle : getDefaultName();
	}

	@Override
	default Component getDisplayName() {
		return getName();
	}
}

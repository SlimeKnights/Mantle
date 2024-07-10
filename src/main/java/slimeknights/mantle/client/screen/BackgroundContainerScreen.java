package slimeknights.mantle.client.screen;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Generic container screen that simply draws the given background
 * @param <T> Container type
 */
@SuppressWarnings("WeakerAccess")
public class BackgroundContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
	/**
	 * Background drawn for this screen
	 */
	protected final ResourceLocation background;

	/**
	 * Creates a new screen instance
	 * @param container  Container class
	 * @param inventory  Player inventory
	 * @param name       Container name
	 * @param background Container background
	 */
	public BackgroundContainerScreen(T container, Inventory inventory, Component name, int height, ResourceLocation background) {
		super(container, inventory, name);
		this.background = background;
		this.imageHeight = height;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		graphics.blit(this.background, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	@RequiredArgsConstructor(staticName = "of")
	public static class Factory<T extends AbstractContainerMenu> implements ScreenConstructor<T,BackgroundContainerScreen<T>> {
		private final ResourceLocation background;
		private final int height;

		/**
		 * Creates a factory from the container name
		 * @param height Screen height
		 * @param name   Name of this container
		 */
		public static <T extends AbstractContainerMenu> Factory<T> ofName(int height, ResourceLocation name) {
			return of(new ResourceLocation(name.getNamespace(), String.format("textures/gui/%s.png", name.getPath())), height);
		}

    @Override
    public BackgroundContainerScreen<T> create(T menu, Inventory inventory, Component title) {
      return new BackgroundContainerScreen<>(menu, inventory, title, height, background);
    }
	}
}

package slimeknights.mantle.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens.Provider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Generic container screen that simply draws the given background
 * @param <T> Container type
 */
@SuppressWarnings("WeakerAccess")
public class BackgroundContainerScreen<T extends ScreenHandler> extends HandledScreen<T> {
	/**
	 * Background drawn for this screen
	 */
	protected final Identifier background;

	/**
	 * Creates a new screen instance
	 * @param container  Container class
	 * @param inventory  Player inventory
	 * @param name       Container name
	 * @param background Container background
	 */
	public BackgroundContainerScreen(T container, PlayerInventory inventory, Text name, int height, Identifier background) {
		super(container, inventory, name);
		this.background = background;
		this.backgroundHeight = height;
		this.playerInventoryTitleY = this.backgroundHeight - 94;
	}

	@Override
	protected void init() {
		super.init();
		this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.drawMouseoverTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void drawBackground(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		assert this.client != null;
		this.client.getTextureManager().bindTexture(this.background);
		this.drawTexture(matrixStack, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}

	public static class Factory<T extends ScreenHandler> implements Provider<T,BackgroundContainerScreen<T>> {
		private final Identifier background;
		private final int height;

		public Factory(Identifier background, int height) {
			this.background = background;
			this.height = height;
		}

		/**
		 * Creates a factory from the container name
		 * @param height Screen height
		 * @param name   Name of this container
		 */
		public static <T extends ScreenHandler> Factory<T> ofName(int height, Identifier name) {
			return new Factory<>(new Identifier(name.getNamespace(), String.format("textures/gui/%s.png", name.getPath())), height);
		}

		@Override
		public BackgroundContainerScreen<T> create(T container, PlayerInventory inventory, Text name) {
			return new BackgroundContainerScreen<>(container, inventory, name, height, background);
		}
	}
}

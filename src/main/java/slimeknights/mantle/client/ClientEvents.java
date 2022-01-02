package slimeknights.mantle.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.settings.AttackIndicatorStatus;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.HandSide;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.model.FallbackModelLoader;
import slimeknights.mantle.client.model.NBTKeyModel;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.connected.ConnectedModel;
import slimeknights.mantle.client.model.fluid.FluidTextureModel;
import slimeknights.mantle.client.model.fluid.FluidsModel;
import slimeknights.mantle.client.model.inventory.InventoryModel;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.data.MantleTags;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.util.OffhandCooldownTracker;

import java.util.function.Function;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEvents {
  private static final Function<OffhandCooldownTracker,Float> COOLDOWN_TRACKER = OffhandCooldownTracker::getCooldown;

  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    IResourceManager manager = Minecraft.getInstance().getResourceManager();
    if (manager instanceof IReloadableResourceManager) {
      ((IReloadableResourceManager)manager).addReloadListener(ModelHelper.LISTENER);
    }
    event.enqueueWork(() -> RegistrationHelper.forEachWoodType(Atlases::addWoodType));
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelRegistryEvent event) {
    // standard models - useful in resource packs for any model
    ModelLoaderRegistry.registerLoader(Mantle.getResource("connected"), ConnectedModel.Loader.INSTANCE);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("item_layer"), MantleItemLayerModel.LOADER);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("colored_block"), ColoredBlockModel.LOADER);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("fallback"), FallbackModelLoader.INSTANCE);

    // NBT dynamic models - require specific data defined in the block/item to use
    ModelLoaderRegistry.registerLoader(Mantle.getResource("nbt_key"), NBTKeyModel.LOADER);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("retextured"), RetexturedModel.Loader.INSTANCE);

    // data models - contain information for other parts in rendering rather than rendering directly
    ModelLoaderRegistry.registerLoader(Mantle.getResource("fluid_texture"), FluidTextureModel.LOADER);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("inventory"), InventoryModel.Loader.INSTANCE);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("fluids"), FluidsModel.Loader.INSTANCE);
  }

  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    IResourceManager manager = Minecraft.getInstance().getResourceManager();
    if (manager instanceof IReloadableResourceManager) {
      ((IReloadableResourceManager)manager).addReloadListener(new BookLoader());
    }
    MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGameOverlayEvent.Post.class, ClientEvents::renderOffhandAttackIndicator);
  }

  // registered with FORGE bus
  private static void renderOffhandAttackIndicator(RenderGameOverlayEvent.Post event) {
    // must have a player, not be in spectator, and have the indicator enabled
    Minecraft minecraft = Minecraft.getInstance();
    GameSettings settings = minecraft.gameSettings;
    if (minecraft.player == null || minecraft.playerController == null || minecraft.playerController.getCurrentGameType() == GameType.SPECTATOR || settings.attackIndicator == AttackIndicatorStatus.OFF) {
      return;
    }

    // enabled if either in the tag, or if force enabled
    boolean tagEnabled = minecraft.player.getHeldItemOffhand().getItem().isIn(MantleTags.Items.OFFHAND_COOLDOWN);
    float cooldown = minecraft.player.getCapability(OffhandCooldownTracker.CAPABILITY).filter(tracker -> tagEnabled || tracker.isForceEnable()).map(COOLDOWN_TRACKER).orElse(1.0f);
    if (cooldown >= 1.0f) {
      return;
    }

    // show attack indicator
    MatrixStack matrixStack = event.getMatrixStack();
    switch (settings.attackIndicator) {
      case CROSSHAIR:
        if (event.getType() == ElementType.CROSSHAIRS && minecraft.gameSettings.getPointOfView().func_243192_a()) {
          if (!settings.showDebugInfo || settings.hideGUI || minecraft.player.hasReducedDebug() || settings.reducedDebugInfo) {
            // mostly cloned from vanilla attack indicator
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int scaledHeight = minecraft.getMainWindow().getScaledHeight();
            // integer division makes this a pain to line up, there might be a simplier version of this formula but I cannot think of one
            int y = (scaledHeight / 2) - 14 + (2 * (scaledHeight % 2));
            int x = minecraft.getMainWindow().getScaledWidth() / 2 - 8;
            int width = (int)(cooldown * 17.0F);
            minecraft.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
            minecraft.ingameGUI.blit(matrixStack, x, y, 36, 94, 16, 4);
            minecraft.ingameGUI.blit(matrixStack, x, y, 52, 94, width, 4);
          }
        }
        break;
      case HOTBAR:
        if (event.getType() == ElementType.HOTBAR && minecraft.renderViewEntity == minecraft.player) {
          int centerWidth = minecraft.getMainWindow().getScaledWidth() / 2;
          int y = minecraft.getMainWindow().getScaledHeight() - 20;
          int x;
          // opposite of the vanilla hand location, extra bit to offset past the offhand slot
          if (minecraft.player.getPrimaryHand() == HandSide.RIGHT) {
            x = centerWidth - 91 - 22 - 32;
          } else {
            x = centerWidth + 91 + 6 + 32;
          }
          minecraft.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
          int l1 = (int)(cooldown * 19.0F);
          RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
          minecraft.ingameGUI.blit(matrixStack, x, y, 0, 94, 18, 18);
          minecraft.ingameGUI.blit(matrixStack, x, y + 18 - l1, 18, 112 - l1, 18, l1);
        }
        break;
    }
  }
}

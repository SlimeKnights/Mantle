package slimeknights.mantle.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
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
import slimeknights.mantle.client.book.repository.FileRepository;
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
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.registration.MantleRegistrations;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.util.OffhandCooldownTracker;

import java.util.function.Function;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEvents {
  private static final Function<OffhandCooldownTracker,Float> COOLDOWN_TRACKER = OffhandCooldownTracker::getCooldown;

  @SubscribeEvent
  static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(MantleRegistrations.SIGN, SignRenderer::new);
  }

  @SubscribeEvent
  static void registerListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(ModelHelper.LISTENER);
    event.registerReloadListener(new BookLoader());
    ResourceColorManager.init(event);
    FluidTooltipHandler.init(event);
  }

  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> RegistrationHelper.forEachWoodType(Sheets::addWoodType));

    BookLoader.registerBook(Mantle.getResource("test"), new FileRepository(Mantle.getResource("books/test")));
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
    MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGameOverlayEvent.PostLayer.class, ClientEvents::renderOffhandAttackIndicator);
  }

  // registered with FORGE bus
  private static void renderOffhandAttackIndicator(RenderGameOverlayEvent.PostLayer event) {
    // must have a player, not be in spectator, and have the indicator enabled
    Minecraft minecraft = Minecraft.getInstance();
    Options settings = minecraft.options;
    if (minecraft.player == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR || settings.attackIndicator == AttackIndicatorStatus.OFF) {
      return;
    }
    IIngameOverlay overlay = event.getOverlay();
    if (overlay != ForgeIngameGui.CROSSHAIR_ELEMENT && overlay != ForgeIngameGui.HOTBAR_ELEMENT) {
      return;
    }

    // enabled if either in the tag, or if force enabled
    float cooldown = minecraft.player.getCapability(OffhandCooldownTracker.CAPABILITY).filter(OffhandCooldownTracker::isEnabled).map(COOLDOWN_TRACKER).orElse(1.0f);
    if (cooldown >= 1.0f) {
      return;
    }

    // show attack indicator
    PoseStack matrixStack = event.getMatrixStack();
    switch (settings.attackIndicator) {
      case CROSSHAIR:
        if (overlay == ForgeIngameGui.CROSSHAIR_ELEMENT && minecraft.options.getCameraType().isFirstPerson()) {
          if (!settings.renderDebug || settings.hideGui || minecraft.player.isReducedDebugInfo() || settings.reducedDebugInfo) {
            // mostly cloned from vanilla attack indicator
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
            // integer division makes this a pain to line up, there might be a simplier version of this formula but I cannot think of one
            int y = (scaledHeight / 2) - 14 + (2 * (scaledHeight % 2));
            int x = minecraft.getWindow().getGuiScaledWidth() / 2 - 8;
            int width = (int)(cooldown * 17.0F);
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            minecraft.gui.blit(matrixStack, x, y, 36, 94, 16, 4);
            minecraft.gui.blit(matrixStack, x, y, 52, 94, width, 4);
          }
        }
        break;
      case HOTBAR:
        if (overlay == ForgeIngameGui.HOTBAR_ELEMENT && minecraft.cameraEntity == minecraft.player) {
          int centerWidth = minecraft.getWindow().getGuiScaledWidth() / 2;
          int y = minecraft.getWindow().getGuiScaledHeight() - 20;
          int x;
          // opposite of the vanilla hand location, extra bit to offset past the offhand slot
          if (minecraft.player.getMainArm() == HumanoidArm.RIGHT) {
            x = centerWidth - 91 - 22 - 32;
          } else {
            x = centerWidth + 91 + 6 + 32;
          }
          RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
          int l1 = (int)(cooldown * 19.0F);
          RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
          minecraft.gui.blit(matrixStack, x, y, 0, 94, 18, 18);
          minecraft.gui.blit(matrixStack, x, y + 18 - l1, 18, 112 - l1, 18, l1);
        }
        break;
    }
  }
}

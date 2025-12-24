package slimeknights.mantle.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.NamedGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.GaugeBlock;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.client.model.FallbackModelLoader;
import slimeknights.mantle.client.model.NBTKeyModel;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.TextureColorHelper;
import slimeknights.mantle.client.model.connected.ConnectedModel;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.render.FluidCuboid;
import slimeknights.mantle.client.render.RenderItem;
import slimeknights.mantle.command.client.MantleClientCommand;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.texture.FluidTextureManager;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.mantle.registration.MantleRegistrations;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.util.OffhandCooldownTracker;
import slimeknights.mantle.util.RegistryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEvents {
  /** Called on construct to initiatlize things that need early entry */
  public static void onConstruct() {}

  @SuppressWarnings("ConstantConditions")
  @SubscribeEvent
  static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
    if (MantleRegistrations.SIGN != null) {
      event.registerBlockEntityRenderer(MantleRegistrations.SIGN, SignRenderer::new);
    }
    if (MantleRegistrations.HANGING_SIGN != null) {
      event.registerBlockEntityRenderer(MantleRegistrations.HANGING_SIGN, HangingSignRenderer::new);
    }
  }

  @SuppressWarnings("removal")
  @SubscribeEvent
  static void registerListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(ModelHelper.LISTENER);
    event.registerReloadListener(new BookLoader());
    ResourceColorManager.init(event);
    FluidTooltipHandler.init(event);
    FluidTextureManager.init(event);
    event.registerReloadListener(FluidCuboid.REGISTRY);
    event.registerReloadListener(RenderItem.REGISTRY);
    event.registerReloadListener(RenderItem.STATE_REGISTRY);
    event.registerReloadListener(TextureColorHelper.RELOAD_LISTENER);
  }

  @SubscribeEvent
  static void clientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> RegistrationHelper.forEachWoodType(Sheets::addWoodType));

    BookLoader.registerBook(Mantle.getResource("test"), new FileRepository(Mantle.getResource("books/test")));
    MantleClientCommand.init();
  }

  @SubscribeEvent
  static void registerModelLoaders(RegisterGeometryLoaders event) {
    // standard models - useful in resource packs for any model
    event.register("connected", ConnectedModel.LOADER);
    event.register("item_layer", MantleItemLayerModel.LOADER);
    event.register("colored_block", ColoredBlockModel.LOADER);
    event.register("fallback", FallbackModelLoader.INSTANCE);

    // NBT dynamic models - require specific data defined in the block/item to use
    event.register("nbt_key", NBTKeyModel.LOADER);
    event.register("retextured", RetexturedModel.LOADER);
  }

  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGuiOverlayEvent.Post.class, ClientEvents::renderOffhandAttackIndicator);
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGuiOverlayEvent.Post.class, ClientEvents::renderGaugeTooltip);
  }

  // registered with FORGE bus
  private static void renderOffhandAttackIndicator(RenderGuiOverlayEvent.Post event) {
    // must have a player, not be in spectator, and have the indicator enabled
    Minecraft minecraft = Minecraft.getInstance();
    Options settings = minecraft.options;
    AttackIndicatorStatus indicator = settings.attackIndicator().get();
    if (minecraft.player == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR || indicator == AttackIndicatorStatus.OFF) {
      return;
    }

    // only care about hotbar and crosshair
    NamedGuiOverlay overlay = event.getOverlay();
    // will be true for hotbar, false for crosshair
    boolean isHotbar = VanillaGuiOverlay.HOTBAR.type() == overlay;
    if (!isHotbar && VanillaGuiOverlay.CROSSHAIR.type() != overlay) {
      return;
    }

    // fetch the current cooldown
    OffhandCooldownTracker tracker = OffhandCooldownTracker.get(minecraft.player);
    if (tracker == null) {
      return;
    }
    float cooldown = tracker.getCooldown();
    if (cooldown >= 1.0f) {
      return;
    }

    // show attack indicator
    GuiGraphics graphics = event.getGuiGraphics();
    switch (indicator) {
      case CROSSHAIR:
        if (!isHotbar && minecraft.options.getCameraType().isFirstPerson()) {
          if (!settings.renderDebug || settings.hideGui || minecraft.player.isReducedDebugInfo() || settings.reducedDebugInfo().get()) {
            // mostly cloned from vanilla attack indicator
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
            // integer division makes this a pain to line up, there might be a simplier version of this formula but I cannot think of one
            int y = (scaledHeight / 2) - 14 + (2 * (scaledHeight % 2));
            int x = minecraft.getWindow().getGuiScaledWidth() / 2 - 8;
            int width = (int)(cooldown * 17.0F);
            graphics.blit(Gui.GUI_ICONS_LOCATION, x, y, 36, 94, 16, 4);
            graphics.blit(Gui.GUI_ICONS_LOCATION, x, y, 52, 94, width, 4);
          }
        }
        break;
      case HOTBAR:
        if (isHotbar && minecraft.cameraEntity == minecraft.player) {
          int centerWidth = minecraft.getWindow().getGuiScaledWidth() / 2;
          int y = minecraft.getWindow().getGuiScaledHeight() - 20;
          int x;
          // opposite of the vanilla hand location, extra bit to offset past the offhand slot
          if (minecraft.player.getMainArm() == HumanoidArm.RIGHT) {
            x = centerWidth - 91 - 22 - 32;
          } else {
            x = centerWidth + 91 + 6 + 32;
          }
//          RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
          int l1 = (int)(cooldown * 19.0F);
          RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
          graphics.blit(Gui.GUI_ICONS_LOCATION, x, y, 0, 94, 18, 18);
          graphics.blit(Gui.GUI_ICONS_LOCATION, x, y + 18 - l1, 18, 112 - l1, 18, l1);
        }
        break;
    }
  }



  /** Renders the tooltip when targeting the gauge block */
  private static void renderGaugeTooltip(RenderGuiOverlayEvent.Post event) {
    if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
      return;
    }
    // must not be in a screen, though chat is fine
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen != null && minecraft.screen.getClass() != ChatScreen.class) {
      return;
    }
    // must have a hit result
    if (minecraft.level == null || minecraft.hitResult == null || minecraft.hitResult.getType() != HitResult.Type.BLOCK) {
      return;
    }
    BlockHitResult blockHit = (BlockHitResult) minecraft.hitResult;
    BlockPos pos = blockHit.getBlockPos();

    // must be targeting a gauge
    BlockState targeted = minecraft.level.getBlockState(blockHit.getBlockPos());
    if (!targeted.is(MantleTags.Blocks.GAUGES)) {
      return;
    }
    BlockEntity gaugeContainer;
    Direction side;
    if (targeted.is(MantleTags.Blocks.ATTACHED_GAUGES)) {
      side = targeted.getValue(BlockStateProperties.FACING);
      gaugeContainer = minecraft.level.getBlockEntity(pos.relative(side.getOpposite()));
    } else {
      side = blockHit.getDirection();
      gaugeContainer = minecraft.level.getBlockEntity(pos);
    }
    // must have a block entity behind the gauge that is not blacklisted
    if (gaugeContainer == null || RegistryHelper.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE, MantleTags.BlockEntities.GAUGE_BLACKLIST, gaugeContainer.getType())) {
      return;
    }
    // block entity must have a fluid handler
    IFluidHandler handler = gaugeContainer.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(EmptyFluidHandler.INSTANCE);
    if (handler.getTanks() <= 0) {
      return;
    }
    // if the fluid is empty, just render the capacity
    FluidStack fluid = handler.getFluidInTank(0);
    List<Component> tooltip;
    if (fluid.isEmpty()) {
      tooltip = List.of(GaugeBlock.formatCapacity(handler.getTankCapacity(0)));
    } else if (RegistryHelper.contains(BuiltInRegistries.BLOCK_ENTITY_TYPE, MantleTags.BlockEntities.HIDES_GAUGE_AMOUNT, gaugeContainer.getType())) {
      // in the tag, don't show capacity
      ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
      tooltip = new ArrayList<>(3);
      tooltip.add(fluid.getDisplayName());
      FluidTooltipHandler.appendAdvanced(id, tooltip);
      tooltip.add(GaugeBlock.formatCapacity(handler.getTankCapacity(0)).withStyle(ChatFormatting.GRAY));
      tooltip.add(FluidTooltipHandler.formatModName(id));
    } else {
      // render full fluid tooltip
      tooltip = FluidTooltipHandler.getFluidTooltip(fluid);
    }

    int x = minecraft.getWindow().getGuiScaledWidth() / 2;
    int y = minecraft.getWindow().getGuiScaledHeight() / 2;
    event.getGuiGraphics().renderTooltip(minecraft.font, tooltip, Optional.empty(), x, y);
  }
}

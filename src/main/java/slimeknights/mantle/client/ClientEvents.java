package slimeknights.mantle.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.GaugeBlock;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.client.model.FallbackModelLoader;
import slimeknights.mantle.client.model.ItemKeyModel;
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
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.util.OffhandCooldownTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEvents {
  /** Called on construct to initiatlize things that need early entry */
  public static void onConstruct() {}

  @SubscribeEvent
  static void registerListeners(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(ModelHelper.LISTENER);
    event.registerReloadListener(new BookLoader());
    ResourceColorManager.init(event);
    FluidTooltipHandler.init(event);
    FluidTextureManager.init(event);
    event.registerReloadListener(FluidCuboid.REGISTRY);
    event.registerReloadListener(RenderItem.REGISTRY);
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
    event.register(ConnectedModel.ID, ConnectedModel.LOADER);
    event.register(MantleItemLayerModel.ID, MantleItemLayerModel.LOADER);
    event.register(ColoredBlockModel.ID, ColoredBlockModel.LOADER);
    event.register(FallbackModelLoader.ID, FallbackModelLoader.INSTANCE);

    // NBT dynamic models - require specific data defined in the block/item to use
    event.register(ItemKeyModel.ID, ItemKeyModel.LOADER);
    event.register(RetexturedModel.ID, RetexturedModel.LOADER);
  }

  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    NeoForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
    NeoForge.EVENT_BUS.addListener(RenderGuiLayerEvent.Post.class, ClientEvents::renderOffhandAttackIndicator);
    NeoForge.EVENT_BUS.addListener(RenderGuiLayerEvent.Post.class, ClientEvents::renderGaugeTooltip);
  }

  // registered with FORGE bus
  /** Renders the offhand attack indicator. Based on {@link Gui#renderCrosshair(GuiGraphics, DeltaTracker)} and {@link Gui#renderItemHotbar(GuiGraphics, DeltaTracker)} */
  private static void renderOffhandAttackIndicator(RenderGuiLayerEvent.Post event) {
    // must have a player, not be in spectator, and have the indicator enabled
    Minecraft minecraft = Minecraft.getInstance();
    Options settings = minecraft.options;
    AttackIndicatorStatus indicator = settings.attackIndicator().get();
    if (minecraft.player == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR || indicator == AttackIndicatorStatus.OFF) {
      return;
    }

    // only care about hotbar and crosshair
    ResourceLocation name = event.getName();
    // will be true for hotbar, false for crosshair
    boolean isHotbar = VanillaGuiLayers.HOTBAR == name;
    if (!isHotbar && VanillaGuiLayers.CROSSHAIR != name) {
      return;
    }

    // fetch the current cooldown
    OffhandCooldownTracker tracker = OffhandCooldownTracker.get(minecraft.player);
    float cooldown = tracker.getCooldown();
    if (cooldown >= 1.0f) {
      return;
    }

    // show attack indicator
    GuiGraphics graphics = event.getGuiGraphics();
    switch (indicator) {
      case CROSSHAIR:
        if (!isHotbar && minecraft.options.getCameraType().isFirstPerson()) {
          if (!minecraft.gui.getDebugOverlay().showDebugScreen() || minecraft.player.isReducedDebugInfo() || settings.reducedDebugInfo().get()) {
            // mostly cloned from vanilla attack indicator
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int scaledHeight = minecraft.getWindow().getGuiScaledHeight();
            // integer division makes this a pain to line up, there might be a simplier version of this formula, but I cannot think of one
            int y = (scaledHeight / 2) - 14 + (2 * (scaledHeight % 2));
            int x = minecraft.getWindow().getGuiScaledWidth() / 2 - 8;
            int width = (int)(cooldown * 17.0F);
            graphics.blitSprite(Gui.CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 16, 4);
            graphics.blitSprite(Gui.CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, x, y, width, 4);
            RenderSystem.defaultBlendFunc();
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
          int height = (int)(cooldown * 19.0F);
          graphics.blitSprite(Gui.HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 18, 18);
          graphics.blitSprite(Gui.HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - height, x, y + 18 - height, 18, height);
        }
        break;
    }
  }



  /** Renders the tooltip when targeting the gauge block */
  private static void renderGaugeTooltip(RenderGuiLayerEvent.Post event) {
    // those RLs are passed into the event constructor, so instance comparison should be fine
    if (event.getName() != VanillaGuiLayers.CROSSHAIR) {
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
    BlockState fluidBlock = targeted;
    BlockPos fluidPos = pos;
    Direction side;
    if (targeted.is(MantleTags.Blocks.ATTACHED_GAUGES)) {
      side = targeted.getValue(BlockStateProperties.FACING);
      fluidPos = pos.relative(side.getOpposite());
      fluidBlock = minecraft.level.getBlockState(fluidPos);
    } else {
      side = blockHit.getDirection();
    }
    // targeted block must not be blacklisted
    if (fluidBlock.is(MantleTags.Blocks.GAUGE_BLACKLIST)) {
      return;
    }
    // block entity must have a fluid handler
    IFluidHandler handler = minecraft.level.getCapability(Capabilities.FluidHandler.BLOCK, fluidPos, side);
    if (handler == null || handler.getTanks() <= 0) {
      return;
    }
    // if the fluid is empty, just render the capacity
    FluidStack fluid = handler.getFluidInTank(0);
    List<Component> tooltip;
    if (fluid.isEmpty()) {
      tooltip = List.of(GaugeBlock.formatCapacity(handler.getTankCapacity(0)));
    } else if (fluidBlock.is(MantleTags.Blocks.HIDES_GAUGE_AMOUNT)) {
      // in the tag, don't show capacity
      ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
      tooltip = new ArrayList<>(3);
      tooltip.add(fluid.getHoverName());
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

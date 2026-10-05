package slimeknights.mantle;

import com.mojang.serialization.MapCodec;
import net.minecraft.Util;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.client.ClientEvents;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.command.argument.ResourceOrTagKeyArgument;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.mantle.data.predicate.block.BlockPropertiesPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.mantle.data.predicate.damage.DamageTypePredicate;
import slimeknights.mantle.data.predicate.damage.SourceAttackerPredicate;
import slimeknights.mantle.data.predicate.damage.SourceMessagePredicate;
import slimeknights.mantle.data.predicate.entity.BlockAtEntityPredicate;
import slimeknights.mantle.data.predicate.entity.EntityPredicate;
import slimeknights.mantle.data.predicate.entity.HasEnchantmentEntityPredicate;
import slimeknights.mantle.data.predicate.entity.HasMobEffectPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityEntityPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.mantle.data.predicate.fluid.FluidPredicate;
import slimeknights.mantle.data.predicate.fluid.FluidTypePredicate;
import slimeknights.mantle.data.predicate.item.ItemPredicate;
import slimeknights.mantle.datagen.MantleBlockTagProvider;
import slimeknights.mantle.datagen.MantleFluidTagProvider;
import slimeknights.mantle.datagen.MantleFluidTooltipProvider;
import slimeknights.mantle.datagen.MantleFluidTransferProvider;
import slimeknights.mantle.datagen.MantleMenuTagProvider;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.transfer.EmptyFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.EmptyFluidCopyDataTransfer;
import slimeknights.mantle.fluid.transfer.EmptyPotionTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidCopyDataTransfer;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer;
import slimeknights.mantle.item.LecternBookItem;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.loot.injection.LootTableInjector;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.recipe.condition.TagCombinationCondition;
import slimeknights.mantle.recipe.condition.TagEmptyCondition;
import slimeknights.mantle.recipe.condition.TagFilledCondition;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.registration.MantleData;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.adapter.RegistryAdapter;
import slimeknights.mantle.util.OffhandCooldownTracker;

import java.util.concurrent.CompletableFuture;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(Mantle.modId)
public class Mantle {
  public static final String modId = "mantle";
  public static final Logger logger = LogManager.getLogger("Mantle");
  /** Namespace for common tags, used for easier migration to the future "c" standard */
  public static final String COMMON = "c";

  /* Instance of this mod, used for grabbing prototype fields */
  public static Mantle instance;

  /* Proxies for sides, used for graphics processing */
  public Mantle(IEventBus modEventBus, ModContainer modContainer) {
    modContainer.registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
    modContainer.registerConfig(Type.SERVER, Config.SERVER_SPEC);

    FluidContainerTransferManager.INSTANCE.init();
    MantleTags.init();

    instance = this;
    modEventBus.addListener(FMLCommonSetupEvent.class, this::commonSetup);
    modEventBus.addListener(RegisterCapabilitiesEvent.class, this::registerCapabilities);
    modEventBus.addListener(GatherDataEvent.class, this::gatherData);
    modEventBus.addListener(RegisterEvent.class, this::register);
    modEventBus.addListener(BlockEntityTypeAddBlocksEvent.class, this::registerBlockEntityBlocks);
    modEventBus.addListener(RegisterPayloadHandlersEvent.class, MantleNetwork::registerPackets);
    MantleRecipes.init(modEventBus);
    MantleLoot.init(modEventBus);
    MantleData.init(modEventBus);
    NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, LecternBookItem::interactWithBlock);

    if (FMLEnvironment.dist == Dist.CLIENT) {
      ClientEvents.onConstruct();
    }
  }

  private void registerCapabilities(RegisterCapabilitiesEvent event) {
    OffhandCooldownTracker.register(event);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
    MantleCommand.init();
    OffhandCooldownTracker.init();
    TagPreference.init();
    LootTableInjector.init();
  }

  private void register(RegisterEvent event) {
    ResourceKey<?> key = event.getRegistryKey();

    if (key == NeoForgeRegistries.Keys.CONDITION_CODECS) {
      RegistryAdapter<MapCodec<? extends ICondition>> adapter = new RegistryAdapter<>(event.getRegistry(NeoForgeRegistries.Keys.CONDITION_CODECS));
      adapter.register(TagEmptyCondition.CODEC, "tag_empty");
      adapter.register(TagFilledCondition.CODEC, "tag_filled");
      adapter.register(TagCombinationCondition.CODEC, "tag_combination_filled");

    } else if (key == Registries.RECIPE_SERIALIZER) {
      // TODO: ingredient migration
//      CraftingHelper.register(FluidContainerIngredient.ID, FluidContainerIngredient.SERIALIZER);

      // fluid container transfer
      IFluidContainerTransfer.LOADER.register(getResource("fill_item"), FillFluidContainerTransfer.LOADER);
      IFluidContainerTransfer.LOADER.register(getResource("fill_copy_data"), FillFluidCopyDataTransfer.LOADER);
      IFluidContainerTransfer.LOADER.register(getResource("empty_item"), EmptyFluidContainerTransfer.LOADER);
      IFluidContainerTransfer.LOADER.register(getResource("empty_copy_data"), EmptyFluidCopyDataTransfer.LOADER);
      IFluidContainerTransfer.LOADER.register(getResource("empty_potion"), EmptyPotionTransfer.LOADER);

      // predicates
      {
        // block predicates
        BlockPredicate.LOADER.register(getResource("requires_tool"), BlockPredicate.REQUIRES_TOOL.getLoader());
        BlockPredicate.LOADER.register(getResource("blocks_motion"), BlockPredicate.BLOCKS_MOTION.getLoader());
        BlockPredicate.LOADER.register(getResource("can_be_replaced"), BlockPredicate.CAN_BE_REPLACED.getLoader());
        BlockPredicate.LOADER.register(getResource("block_properties"), BlockPropertiesPredicate.LOADER);

        // item predicates
        ItemPredicate.LOADER.register(getResource("has_container"), ItemPredicate.HAS_CONTAINER.getLoader());
        ItemPredicate.LOADER.register(getResource("may_have_transfer"), ItemPredicate.MAY_HAVE_TRANSFER.getLoader());

        // fluid predicates
        FluidPredicate.LOADER.register(getResource("fluid_type"), FluidTypePredicate.LOADER);
        FluidPredicate.LOADER.register(getResource("is_source"), FluidPredicate.SOURCE.getLoader());
        FluidPredicate.LOADER.register(getResource("has_bucket"), FluidPredicate.HAS_BUCKET.getLoader());
        FluidPredicate.LOADER.register(getResource("lighter_than_air"), FluidPredicate.LIGHTER_THAN_AIR.getLoader());

        // entity predicates
        // simple
        EntityPredicate.LOADER.register(getResource("fire_immune"), EntityPredicate.FIRE_IMMUNE.getLoader());
        EntityPredicate.LOADER.register(getResource("can_freeze"), EntityPredicate.CAN_FREEZE.getLoader());
        EntityPredicate.LOADER.register(getResource("on_fire"), EntityPredicate.ON_FIRE.getLoader());
        EntityPredicate.LOADER.register(getResource("is_freezing"), EntityPredicate.IS_FREEZING.getLoader());
        EntityPredicate.LOADER.register(getResource("is_in_powdered_snow"), EntityPredicate.IS_IN_POWDERED_SNOW.getLoader());
        EntityPredicate.LOADER.register(getResource("on_ground"), EntityPredicate.ON_GROUND.getLoader());
        EntityPredicate.LOADER.register(getResource("crouching"), EntityPredicate.CROUCHING.getLoader());
        EntityPredicate.LOADER.register(getResource("sprinting"), EntityPredicate.SPRINTING.getLoader());
        EntityPredicate.LOADER.register(getResource("eyes_in_water"), EntityPredicate.EYES_IN_WATER.getLoader());
        EntityPredicate.LOADER.register(getResource("feet_in_water"), EntityPredicate.FEET_IN_WATER.getLoader());
        EntityPredicate.LOADER.register(getResource("underwater"), EntityPredicate.UNDERWATER.getLoader());
        EntityPredicate.LOADER.register(getResource("raining_at"), EntityPredicate.RAINING.getLoader());
        // living simple
        LivingEntityPredicate.LOADER.register(getResource("water_sensitive"), LivingEntityPredicate.WATER_SENSITIVE.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("blocking"), LivingEntityPredicate.BLOCKING.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("elytra_flying"), LivingEntityPredicate.ELYTRA_FLYING.getLoader());
        // property
        EntityPredicate.LOADER.register(getResource("living"), LivingEntityEntityPredicate.LOADER);
        EntityPredicate.LOADER.register(getResource("block_at_entity"), BlockAtEntityPredicate.LOADER);
        LivingEntityPredicate.LOADER.register(getResource("has_enchantment"), HasEnchantmentEntityPredicate.LOADER);
        LivingEntityPredicate.LOADER.register(getResource("has_effect"), HasMobEffectPredicate.LOADER);

        // damage predicates
        // simple
        DamageSourcePredicate.LOADER.register(getResource("has_entity"), DamageSourcePredicate.HAS_ENTITY.getLoader());
        DamageSourcePredicate.LOADER.register(getResource("is_indirect"), DamageSourcePredicate.IS_INDIRECT.getLoader());
        DamageSourcePredicate.LOADER.register(getResource("can_protect"), DamageSourcePredicate.CAN_PROTECT.getLoader());
        // fields
        DamageSourcePredicate.LOADER.register(getResource("damage_type"), DamageTypePredicate.LOADER);
        DamageSourcePredicate.LOADER.register(getResource("message"), SourceMessagePredicate.LOADER);
        DamageSourcePredicate.LOADER.register(getResource("attacker"), SourceAttackerPredicate.LOADER);
      }
    }
    else if (key == Registries.COMMAND_ARGUMENT_TYPE) {
      RegistryAdapter<ArgumentTypeInfo<?,?>> adapter = new RegistryAdapter<>(event.getRegistry(Registries.COMMAND_ARGUMENT_TYPE));
      ResourceOrTagKeyArgument.Info<?> info = new ResourceOrTagKeyArgument.Info<>();
      adapter.register(info, "resource_or_tag_key");
      ArgumentTypeInfos.registerByClass(RegistrationHelper.genericArgumentType(ResourceOrTagKeyArgument.class), info);
    }
    else {
      MantleLoot.registerGlobalLootModifiers(event);
    }
  }

  private void registerBlockEntityBlocks(BlockEntityTypeAddBlocksEvent event) {
    event.modify(BlockEntityType.SIGN, RegistrationHelper.buildSignBlocks());
    event.modify(BlockEntityType.HANGING_SIGN, RegistrationHelper.buildHangingSignBlocks());
  }

  private void gatherData(final GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    boolean server = event.includeServer();
    boolean client = event.includeClient();
    PackOutput packOutput = generator.getPackOutput();
    CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
    generator.addProvider(server, new MantleBlockTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new MantleFluidTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new MantleMenuTagProvider(packOutput, lookupProvider, existingFileHelper));
    generator.addProvider(server, new MantleFluidTransferProvider(packOutput, lookupProvider));
    generator.addProvider(client, new MantleFluidTooltipProvider(packOutput));
  }

  /**
   * Gets a resource location for Mantle
   * @param name  Name
   * @return  Resource location instance
   */
  public static ResourceLocation getResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(modId, name);
  }

  /**
   * Gets a resource location for the common namespace, which is "forge" for 1.20 and "c" for 1.21.
   * @param name  Name
   * @return  Resource location instance
   */
  public static ResourceLocation commonResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(COMMON, name);
  }

  /**
   * Makes a translation key for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static String makeDescriptionId(String base, String name) {
    return Util.makeDescriptionId(base, getResource(name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static MutableComponent makeComponent(String base, String name) {
    return Component.translatable(makeDescriptionId(base, name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @param args  Additional arguments to format strings
   * @return  Translation key
   */
  public static MutableComponent makeComponent(String base, String name, Object... args) {
    return Component.translatable(makeDescriptionId(base, name), args);
  }
}

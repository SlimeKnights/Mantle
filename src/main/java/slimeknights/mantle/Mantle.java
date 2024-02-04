package slimeknights.mantle;

import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.mantle.data.predicate.block.SetBlockPredicate;
import slimeknights.mantle.data.predicate.block.TagBlockPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.mantle.data.predicate.damage.SourceAttackerPredicate;
import slimeknights.mantle.data.predicate.damage.SourceMessagePredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.mantle.data.predicate.entity.MobTypePredicate;
import slimeknights.mantle.data.predicate.entity.TagEntityPredicate;
import slimeknights.mantle.data.predicate.item.ItemPredicate;
import slimeknights.mantle.data.predicate.item.ItemSetPredicate;
import slimeknights.mantle.data.predicate.item.ItemTagPredicate;
import slimeknights.mantle.datagen.MantleFluidTagProvider;
import slimeknights.mantle.datagen.MantleFluidTooltipProvider;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.transfer.EmptyFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.EmptyFluidWithNBTTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidWithNBTTransfer;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import slimeknights.mantle.item.LecternBookItem;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.recipe.crafting.ShapedFallbackRecipe;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.helper.TagEmptyCondition;
import slimeknights.mantle.recipe.helper.TagPreference;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.registration.adapter.BlockEntityTypeRegistryAdapter;
import slimeknights.mantle.registration.adapter.RegistryAdapter;
import slimeknights.mantle.util.OffhandCooldownTracker;

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

  /* Instance of this mod, used for grabbing prototype fields */
  public static Mantle instance;

  /* Proxies for sides, used for graphics processing */
  public Mantle() {
    ModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
    ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_SPEC);

    FluidContainerTransferManager.INSTANCE.init();
    MantleTags.init();

    instance = this;
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(EventPriority.NORMAL, false, FMLCommonSetupEvent.class, this::commonSetup);
    bus.addListener(EventPriority.NORMAL, false, RegisterCapabilitiesEvent.class, this::registerCapabilities);
    bus.addListener(EventPriority.NORMAL, false, GatherDataEvent.class, this::gatherData);
    bus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
    bus.addGenericListener(BlockEntityType.class, this::registerBlockEntities);
    bus.addGenericListener(GlobalLootModifierSerializer.class, MantleLoot::registerGlobalLootModifiers);
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerInteractEvent.RightClickBlock.class, LecternBookItem::interactWithBlock);
  }

  private void registerCapabilities(RegisterCapabilitiesEvent event) {
    OffhandCooldownTracker.register(event);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
    MantleNetwork.registerPackets();
    MantleCommand.init();
    OffhandCooldownTracker.init();
    TagPreference.init();
  }

  private void registerRecipeSerializers(final RegistryEvent.Register<RecipeSerializer<?>> event) {
    RegistryAdapter<RecipeSerializer<?>> adapter = new RegistryAdapter<>(event.getRegistry());
    adapter.register(new ShapedFallbackRecipe.Serializer(), "crafting_shaped_fallback");
    adapter.register(new ShapedRetexturedRecipe.Serializer(), "crafting_shaped_retextured");

    CraftingHelper.register(TagEmptyCondition.SERIALIZER);
    CraftingHelper.register(FluidContainerIngredient.ID, FluidContainerIngredient.SERIALIZER);

    // fluid container transfer
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(EmptyFluidContainerTransfer.ID, EmptyFluidContainerTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(FillFluidContainerTransfer.ID, FillFluidContainerTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(EmptyFluidWithNBTTransfer.ID, EmptyFluidWithNBTTransfer.DESERIALIZER);
    FluidContainerTransferManager.TRANSFER_LOADERS.registerDeserializer(FillFluidWithNBTTransfer.ID, FillFluidWithNBTTransfer.DESERIALIZER);

    // predicates
    {
      // block predicates
      BlockPredicate.LOADER.register(getResource("and"), BlockPredicate.AND);
      BlockPredicate.LOADER.register(getResource("or"), BlockPredicate.OR);
      BlockPredicate.LOADER.register(getResource("inverted"), BlockPredicate.INVERTED);
      BlockPredicate.LOADER.register(getResource("requires_tool"), BlockPredicate.REQUIRES_TOOL.getLoader());
      BlockPredicate.LOADER.register(getResource("set"), SetBlockPredicate.LOADER);
      BlockPredicate.LOADER.register(getResource("tag"), TagBlockPredicate.LOADER);

      // item predicates
      ItemPredicate.LOADER.register(getResource("and"), ItemPredicate.AND);
      ItemPredicate.LOADER.register(getResource("or"), ItemPredicate.OR);
      ItemPredicate.LOADER.register(getResource("inverted"), ItemPredicate.INVERTED);
      ItemPredicate.LOADER.register(getResource("any"), ItemPredicate.ANY.getLoader());
      ItemPredicate.LOADER.register(getResource("set"), ItemSetPredicate.LOADER);
      ItemPredicate.LOADER.register(getResource("tag"), ItemTagPredicate.LOADER);

      // entity predicates
      LivingEntityPredicate.LOADER.register(getResource("and"), LivingEntityPredicate.AND);
      LivingEntityPredicate.LOADER.register(getResource("or"), LivingEntityPredicate.OR);
      LivingEntityPredicate.LOADER.register(getResource("inverted"), LivingEntityPredicate.INVERTED);
      LivingEntityPredicate.LOADER.register(getResource("any"), LivingEntityPredicate.ANY.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("fire_immune"), LivingEntityPredicate.FIRE_IMMUNE.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("water_sensitive"), LivingEntityPredicate.WATER_SENSITIVE.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("on_fire"), LivingEntityPredicate.ON_FIRE.getLoader());
      LivingEntityPredicate.LOADER.register(getResource("tag"), TagEntityPredicate.LOADER);
      LivingEntityPredicate.LOADER.register(getResource("mob_type"), MobTypePredicate.LOADER);
      // register mob types
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("undefined"), MobType.UNDEFINED);
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("undead"), MobType.UNDEAD);
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("arthropod"), MobType.ARTHROPOD);
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("illager"), MobType.ILLAGER);
      MobTypePredicate.MOB_TYPES.register(new ResourceLocation("water"), MobType.WATER);

      // damage predicates
      DamageSourcePredicate.LOADER.register(getResource("and"), DamageSourcePredicate.AND);
      DamageSourcePredicate.LOADER.register(getResource("or"), DamageSourcePredicate.OR);
      DamageSourcePredicate.LOADER.register(getResource("inverted"), DamageSourcePredicate.INVERTED);
      DamageSourcePredicate.LOADER.register(getResource("any"), DamageSourcePredicate.ANY.getLoader());
      // vanilla properties
      DamageSourcePredicate.LOADER.register(getResource("projectile"), DamageSourcePredicate.PROJECTILE.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("explosion"), DamageSourcePredicate.EXPLOSION.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("bypass_armor"), DamageSourcePredicate.BYPASS_ARMOR.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("damage_helmet"), DamageSourcePredicate.DAMAGE_HELMET.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("bypass_invulnerable"), DamageSourcePredicate.BYPASS_INVULNERABLE.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("bypass_magic"), DamageSourcePredicate.BYPASS_MAGIC.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("fire"), DamageSourcePredicate.FIRE.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("magic"), DamageSourcePredicate.MAGIC.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("fall"), DamageSourcePredicate.FALL.getLoader());
      // custom
      DamageSourcePredicate.LOADER.register(getResource("can_protect"), DamageSourcePredicate.CAN_PROTECT.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("melee"), DamageSourcePredicate.MELEE.getLoader());
      DamageSourcePredicate.LOADER.register(getResource("message"), SourceMessagePredicate.LOADER);
      DamageSourcePredicate.LOADER.register(getResource("attacker"), SourceAttackerPredicate.LOADER);

    }
  }

  private void registerBlockEntities(final RegistryEvent.Register<BlockEntityType<?>> event) {
    BlockEntityTypeRegistryAdapter adapter = new BlockEntityTypeRegistryAdapter(event.getRegistry());
    adapter.register(MantleSignBlockEntity::new, "sign", MantleSignBlockEntity::buildSignBlocks);
  }

  private void gatherData(final GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    if (event.includeServer()) {
      generator.addProvider(new MantleFluidTagProvider(generator, event.getExistingFileHelper()));
    }
    if (event.includeClient()) {
      generator.addProvider(new MantleFluidTooltipProvider(generator));
    }
  }

  /**
   * Gets a resource location for Mantle
   * @param name  Name
   * @return  Resource location instance
   */
  public static ResourceLocation getResource(String name) {
    return new ResourceLocation(modId, name);
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
    return new TranslatableComponent(makeDescriptionId(base, name));
  }
}

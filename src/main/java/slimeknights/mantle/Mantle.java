package slimeknights.mantle;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.block.entity.MantleSignBlockEntity;
import slimeknights.mantle.client.ClientEvents;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.mantle.data.predicate.block.BlockPropertiesPredicate;
import slimeknights.mantle.data.predicate.block.SetBlockPredicate;
import slimeknights.mantle.data.predicate.block.TagBlockPredicate;
import slimeknights.mantle.data.predicate.damage.DamageSourcePredicate;
import slimeknights.mantle.data.predicate.damage.SourceAttackerPredicate;
import slimeknights.mantle.data.predicate.damage.SourceMessagePredicate;
import slimeknights.mantle.data.predicate.entity.EntitySetPredicate;
import slimeknights.mantle.data.predicate.entity.HasEnchantmentEntityPredicate;
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

import java.util.Objects;

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
    bus.addListener(EventPriority.NORMAL, false, RegisterEvent.class, this::register);
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerInteractEvent.RightClickBlock.class, LecternBookItem::interactWithBlock);

    if (FMLEnvironment.dist == Dist.CLIENT) {
      ClientEvents.onConstruct();
    }
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

  private void register(RegisterEvent event) {
    ResourceKey<?> key = event.getRegistryKey();
    if (key == Registry.RECIPE_SERIALIZER_REGISTRY) {
      RegistryAdapter<RecipeSerializer<?>> adapter = new RegistryAdapter<>(Objects.requireNonNull(event.getForgeRegistry()));
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
        BlockPredicate.LOADER.register(getResource("any"), BlockPredicate.ANY.getLoader());
        BlockPredicate.LOADER.register(getResource("requires_tool"), BlockPredicate.REQUIRES_TOOL.getLoader());
        BlockPredicate.LOADER.register(getResource("set"), SetBlockPredicate.LOADER);
        BlockPredicate.LOADER.register(getResource("tag"), TagBlockPredicate.LOADER);
        BlockPredicate.LOADER.register(getResource("block_properties"), BlockPropertiesPredicate.LOADER);

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
        // simple
        LivingEntityPredicate.LOADER.register(getResource("any"), LivingEntityPredicate.ANY.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("fire_immune"), LivingEntityPredicate.FIRE_IMMUNE.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("water_sensitive"), LivingEntityPredicate.WATER_SENSITIVE.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("on_fire"), LivingEntityPredicate.ON_FIRE.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("on_ground"), LivingEntityPredicate.ON_GROUND.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("crouching"), LivingEntityPredicate.CROUCHING.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("eyes_in_water"), LivingEntityPredicate.EYES_IN_WATER.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("feet_in_water"), LivingEntityPredicate.FEET_IN_WATER.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("underwater"), LivingEntityPredicate.UNDERWATER.getLoader());
        LivingEntityPredicate.LOADER.register(getResource("raining_at"), LivingEntityPredicate.RAINING.getLoader());
        // property
        LivingEntityPredicate.LOADER.register(getResource("set"), EntitySetPredicate.LOADER);
        LivingEntityPredicate.LOADER.register(getResource("tag"), TagEntityPredicate.LOADER);
        LivingEntityPredicate.LOADER.register(getResource("mob_type"), MobTypePredicate.LOADER);
        LivingEntityPredicate.LOADER.register(getResource("has_enchantment"), HasEnchantmentEntityPredicate.LOADER);
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
    else if (key == Registry.BLOCK_ENTITY_TYPE_REGISTRY) {
      BlockEntityTypeRegistryAdapter adapter = new BlockEntityTypeRegistryAdapter(Objects.requireNonNull(event.getForgeRegistry()));
      adapter.register(MantleSignBlockEntity::new, "sign", MantleSignBlockEntity::buildSignBlocks);
    }
    else if (key == ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS) {
      MantleLoot.registerGlobalLootModifiers(event);
    }
  }

  private void gatherData(final GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    boolean server = event.includeServer();
    boolean client = event.includeClient();
    generator.addProvider(server, new MantleFluidTagProvider(generator, event.getExistingFileHelper()));
    generator.addProvider(client, new MantleFluidTooltipProvider(generator));
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
    return Component.translatable(makeDescriptionId(base, name));
  }
}

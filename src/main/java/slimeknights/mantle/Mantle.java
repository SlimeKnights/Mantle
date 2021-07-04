package slimeknights.mantle;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.data.MantleItemTagProvider;
import slimeknights.mantle.data.MantleTags;
import slimeknights.mantle.item.LecternBookItem;
import slimeknights.mantle.loot.AddEntryLootModifier;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.loot.ReplaceItemLootModifier;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.recipe.crafting.ShapedFallbackRecipe;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.ingredient.IngredientIntersection;
import slimeknights.mantle.recipe.ingredient.IngredientWithout;
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

    instance = this;
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(EventPriority.NORMAL, false, FMLCommonSetupEvent.class, this::commonSetup);
    bus.addListener(EventPriority.NORMAL, false, GatherDataEvent.class, this::gatherData);
    bus.addListener(EventPriority.NORMAL, false, ModConfig.ModConfigEvent.class, Config::configChanged);
    bus.addGenericListener(IRecipeSerializer.class, this::registerRecipeSerializers);
    bus.addGenericListener(GlobalLootModifierSerializer.class, this::registerGlobalLootModifiers);
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerInteractEvent.RightClickBlock.class, LecternBookItem::interactWithBlock);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
    MantleNetwork.registerPackets();
    MantleCommand.init();
    OffhandCooldownTracker.register();
    MantleTags.init();
  }

  private void gatherData(final GatherDataEvent event) {
    if (event.includeServer()) {
      DataGenerator dataGenerator = event.getGenerator();
      dataGenerator.addProvider(new MantleItemTagProvider(dataGenerator, event.getExistingFileHelper()));
    }
  }

  private void registerRecipeSerializers(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
    RegistryAdapter<IRecipeSerializer<?>> adapter = new RegistryAdapter<>(event.getRegistry());
    adapter.register(new ShapedFallbackRecipe.Serializer(), "crafting_shaped_fallback");
    adapter.register(new ShapedRetexturedRecipe.Serializer(), "crafting_shaped_retextured");

    CraftingHelper.register(IngredientWithout.ID, IngredientWithout.SERIALIZER);
    CraftingHelper.register(IngredientIntersection.ID, IngredientIntersection.SERIALIZER);

    // done here as no dedicated event
    MantleLoot.register();
  }

  private void registerGlobalLootModifiers(final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
    RegistryAdapter<GlobalLootModifierSerializer<?>> adapter = new RegistryAdapter<>(event.getRegistry());
    adapter.register(new AddEntryLootModifier.Serializer(), "add_entry");
    adapter.register(new ReplaceItemLootModifier.Serializer(), "replace_item");
  }

  /**
   * Gets a resource location for Mantle
   * @param name  Name
   * @return  Resource location instance
   */
  public static ResourceLocation getResource(String name) {
    return new ResourceLocation(modId, name);
  }
}

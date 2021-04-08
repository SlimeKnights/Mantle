package slimeknights.mantle;

import net.fabricmc.api.ModInitializer;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.loot.AddEntryLootModifier;
import slimeknights.mantle.loot.MantleLoot;
import slimeknights.mantle.loot.ReplaceItemLootModifier;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.recipe.crafting.ShapedFallbackRecipe;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.ingredient.IngredientIntersection;
import slimeknights.mantle.recipe.ingredient.IngredientWithout;
import slimeknights.mantle.registration.adapter.RegistryAdapter;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public class Mantle implements ModInitializer {

  public static final String modId = "mantle";
  public static final Logger logger = LogManager.getLogger("Mantle");

  /* Instance of this mod, used for grabbing prototype fields */
  public static Mantle instance;

  @Override
  public void onInitialize() {
    MantleNetwork.registerPackets();
  }

  public Mantle() {
//    ModLoadingContext.get().registerConfig(Type.CLIENT, Config.CLIENT_SPEC);
//    ModLoadingContext.get().registerConfig(Type.SERVER, Config.SERVER_SPEC);

    instance = this;
//    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::commonSetup);
    bus.addListener(Config::configChanged);
    bus.addGenericListener(RecipeSerializer.class, this::registerRecipeSerializers);
    bus.addGenericListener(GlobalLootModifierSerializer.class, this::registerGlobalLootModifiers);
  }

  private void registerRecipeSerializers(final RegistryEvent.Register<RecipeSerializer<?>> event) {
    RegistryAdapter<RecipeSerializer<?>> adapter = new RegistryAdapter<>(event.getRegistry());
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
   * @param name Name
   * @return Identifier instance
   */
  public static Identifier getResource(String name) {
    return new Identifier(modId, name);
  }
}

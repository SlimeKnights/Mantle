package slimeknights.mantle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.model.FallbackModelLoader;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.connected.ConnectedModel;
import slimeknights.mantle.client.model.fluid.FluidsModel;
import slimeknights.mantle.client.model.inventory.InventoryModel;
import slimeknights.mantle.client.model.util.ModelHelper;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEvents {

  public static void onConstruct() {
    //noinspection ConstantConditions  Minecraft is null during data gen, causing loader registration to fail
    if (Minecraft.getInstance() != null) {
      // register on construct as sometimes models start loading before client setup or model regsitry event
      ModelLoaderRegistry.registerLoader(Mantle.getResource("fallback"), FallbackModelLoader.INSTANCE);
      ModelLoaderRegistry.registerLoader(Mantle.getResource("inventory"), InventoryModel.Loader.INSTANCE);
      ModelLoaderRegistry.registerLoader(Mantle.getResource("connected"), ConnectedModel.Loader.INSTANCE);
      ModelLoaderRegistry.registerLoader(Mantle.getResource("fluids"), FluidsModel.Loader.INSTANCE);
      ModelLoaderRegistry.registerLoader(Mantle.getResource("retextured"), RetexturedModel.Loader.INSTANCE);

      IResourceManager manager = Minecraft.getInstance().getResourceManager();
      if (manager instanceof IReloadableResourceManager) {
        ((IReloadableResourceManager)manager).addReloadListener(ModelHelper.LISTENER);
      }
    }
  }

  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    IResourceManager manager = Minecraft.getInstance().getResourceManager();
    if (manager instanceof IReloadableResourceManager) {
      ((IReloadableResourceManager)manager).addReloadListener(new BookLoader());
    }
    MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
  }
}

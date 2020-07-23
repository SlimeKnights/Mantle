package slimeknights.mantle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.model.inventory.InventoryModel;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Mantle.modId, value = Dist.CLIENT, bus = Bus.MOD)
public class ClientEvents {
  @SubscribeEvent
  static void commonSetup(FMLCommonSetupEvent event) {
    IResourceManager manager = Minecraft.getInstance().getResourceManager();
    if (manager instanceof IReloadableResourceManager) {
      ((IReloadableResourceManager)manager).addReloadListener(new BookLoader());
    }
    MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelRegistryEvent event) {
    ModelLoaderRegistry.registerLoader(Mantle.getResource("inventory"), InventoryModel.Loader.INSTANCE);
  }
}

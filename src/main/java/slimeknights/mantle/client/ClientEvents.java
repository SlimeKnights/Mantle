package slimeknights.mantle.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.model.FallbackModelLoader;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.connected.ConnectedModel;
import slimeknights.mantle.client.model.fluid.FluidsModel;
import slimeknights.mantle.client.model.inventory.InventoryModel;
import slimeknights.mantle.client.model.util.ModelHelper;

@SuppressWarnings("unused")
public class ClientEvents implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
    if (manager instanceof ReloadableResourceManager) {
      ((ReloadableResourceManager)manager).registerListener(ModelHelper.LISTENER);
    }

    ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
    if (manager instanceof ReloadableResourceManager) {
      ((ReloadableResourceManager)manager).registerListener(new BookLoader());
    }
    MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());

    ModelLoaderRegistry.registerLoader(Mantle.getResource("fallback"), FallbackModelLoader.INSTANCE);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("inventory"), InventoryModel.Loader.INSTANCE);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("connected"), ConnectedModel.Loader.INSTANCE);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("fluids"), FluidsModel.Loader.INSTANCE);
    ModelLoaderRegistry.registerLoader(Mantle.getResource("retextured"), RetexturedModel.Loader.INSTANCE);
  }
}

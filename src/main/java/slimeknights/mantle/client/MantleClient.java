package slimeknights.mantle.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.model.util.ModelHelper;

@SuppressWarnings("unused")
public class MantleClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    ResourceManager manager = MinecraftClient.getInstance().getResourceManager();
    if (manager instanceof ReloadableResourceManager) {
      ((ReloadableResourceManager)manager).registerListener(ModelHelper.LISTENER);
    }

    if (manager instanceof ReloadableResourceManager) {
      ((ReloadableResourceManager)manager).registerListener(new BookLoader());
    }

//    ModelLoaderRegistry.registerLoader(Mantle.getResource("fallback"), FallbackModelLoader.INSTANCE);
//    ModelLoaderRegistry.registerLoader(Mantle.getResource("inventory"), InventoryModel.Loader.INSTANCE);
//    ModelLoaderRegistry.registerLoader(Mantle.getResource("connected"), ConnectedModel.Loader.INSTANCE);
//    ModelLoaderRegistry.registerLoader(Mantle.getResource("fluids"), FluidsModel.Loader.INSTANCE);
//    ModelLoaderRegistry.registerLoader(Mantle.getResource("retextured"), RetexturedModel.Loader.INSTANCE);
  }
}

package slimeknights.mantle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.common.ServerProxy;

public class ClientProxy extends ServerProxy {

  private BookLoader bookLoader;

  @Override
  public void preInit() {
    this.bookLoader = new BookLoader();
  }

  @Override
  public void init() {
    this.bookLoader.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
  }

  @Override
  public void postInit() {
    ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this.bookLoader);
    MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
  }

}

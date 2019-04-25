package slimeknights.mantle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.common.CommonProxy;

public class ClientProxy extends CommonProxy {

    private BookLoader bookLoader;

    @Override
    public void preInit() {
        bookLoader = new BookLoader();
    }

    @Override
    public void init() {
        bookLoader.onResourceManagerReload(Minecraft.getInstance().getResourceManager());
    }

    @Override
    public void postInit() {
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(bookLoader);
        MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
    }
}

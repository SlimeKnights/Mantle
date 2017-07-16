package slimeknights.mantle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;

import java.awt.print.Book;

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
        bookLoader.onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
    }

    @Override
    public void postInit() {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(bookLoader);
        MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
    }
}

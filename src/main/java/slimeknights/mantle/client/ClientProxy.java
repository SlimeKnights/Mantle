package slimeknights.mantle.client;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.common.CommonProxy;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        new BookLoader();
    }
}

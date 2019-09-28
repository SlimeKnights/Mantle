package slimeknights.mantle.network;

import slimeknights.mantle.network.book.UpdateSavedPagePacket;

public class BookNetwork {
    public static final NetworkWrapper wrapper = new NetworkWrapper("mantle:books");

    public static void registerPackets() {
        wrapper.registerPacket(UpdateSavedPagePacket.class, UpdateSavedPagePacket::encode, UpdateSavedPagePacket::new, UpdateSavedPagePacket::handle);
    }
}

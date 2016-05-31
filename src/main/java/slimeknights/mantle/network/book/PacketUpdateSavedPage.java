package slimeknights.mantle.network.book;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import io.netty.buffer.ByteBuf;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.network.AbstractPacket;

public class PacketUpdateSavedPage extends AbstractPacket {

  private String pageName;

  public PacketUpdateSavedPage() {

  }

  public PacketUpdateSavedPage(String pageName) {
    this.pageName = pageName;
  }

  @Override
  public IMessage handleClient(NetHandlerPlayClient netHandler) {
    return null;
  }

  @Override
  public IMessage handleServer(NetHandlerPlayServer netHandler) {
    if (netHandler.playerEntity != null && pageName != null) {
      EntityPlayer player = netHandler.playerEntity;

      ItemStack is = player.getHeldItem(EnumHand.MAIN_HAND);

      if(is != null) {
        BookHelper.writeSavedPage(is, pageName);
      }
    }

    return null;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    pageName = ByteBufUtils.readUTF8String(buf);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    ByteBufUtils.writeUTF8String(buf, pageName);
  }
}

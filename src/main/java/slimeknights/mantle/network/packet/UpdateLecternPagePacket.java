package slimeknights.mantle.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.mantle.util.BlockEntityHelper;

/**
 * Packet to update the book page in a lectern
 */
public record UpdateLecternPagePacket(BlockPos pos, String page) implements ISimplePacket {
  public static final Type<UpdateLecternPagePacket> TYPE = new Type<>(Mantle.getResource("update_lectern_page"));
  public static final StreamCodec<ByteBuf, UpdateLecternPagePacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, UpdateLecternPagePacket::pos,
    ByteBufCodecs.stringUtf8(100), UpdateLecternPagePacket::page,
    UpdateLecternPagePacket::new);

  @Override
  public Type<UpdateLecternPagePacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    if (this.page != null) {
      Player player = context.player();
      Level world = player.getCommandSenderWorld();
      if (BlockEntityHelper.isBlockLoaded(world, pos)) {
        if (world.getBlockEntity(pos) instanceof LecternBlockEntity te) {
          ItemStack stack = te.getBook();
          if (!stack.isEmpty()) {
            BookHelper.writeSavedPageToBook(stack, this.page);
          }
        } else {
          Mantle.logger.error("Failed to find lectern at {} to update page for {}.", pos, player.getScoreboardName());
        }
      } else {
        Mantle.logger.error("Attempted to update lectern page at {} for {}, but world is not loaded", pos, player.getScoreboardName());
      }
    }
  }
}

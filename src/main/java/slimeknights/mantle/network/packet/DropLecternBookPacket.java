package slimeknights.mantle.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.ISimplePacket;

/** Packet to drop the book as item from lectern. */
public record DropLecternBookPacket(BlockPos pos) implements ISimplePacket {
  public static final Type<DropLecternBookPacket> TYPE = new Type<>(Mantle.getResource("drop_lectern_book"));
  public static final StreamCodec<ByteBuf, DropLecternBookPacket> CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, DropLecternBookPacket::pos,
    DropLecternBookPacket::new);

  @Override
  public Type<DropLecternBookPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    Player player = context.player();
    Level world = player.level();
    if (world.hasChunkAt(pos)) {
      BlockState state = world.getBlockState(pos);
      if (state.getBlock() instanceof LecternBlock && state.getValue(LecternBlock.HAS_BOOK)) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof LecternBlockEntity lecternTe) {
          ItemStack book = lecternTe.getBook().copy();
          if (!book.isEmpty()) {
            if (!player.addItem(book)) {
              player.drop(book, false, false);
            }

            lecternTe.clearContent();

            // fix lectern state
            world.setBlock(pos, state.setValue(LecternBlock.POWERED, false).setValue(LecternBlock.HAS_BOOK, false), 3);
            world.updateNeighborsAt(pos.below(), state.getBlock());
          }
        }
      }
    }
  }
}

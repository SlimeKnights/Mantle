package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;

/**
 * Packet to drop the book as item from lectern
 */
public record DropLecternBookPacket(BlockPos pos) implements IThreadsafePacket {
  public static final CustomPacketPayload.Type<DropLecternBookPacket> TYPE = new CustomPacketPayload.Type<>(Mantle.getResource("drop_lectern_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, DropLecternBookPacket> STREAM_CODEC = StreamCodec.composite(
    BlockPos.STREAM_CODEC, DropLecternBookPacket::pos,
    DropLecternBookPacket::new
  );

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void handleThreadsafe(IPayloadContext context) {
    Player contextPlayer = context.player();
    if (!(contextPlayer instanceof ServerPlayer player)) {
      return;
    }

    ServerLevel world = player.serverLevel();
    if (!world.hasChunkAt(pos)) {
      return;
    }

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

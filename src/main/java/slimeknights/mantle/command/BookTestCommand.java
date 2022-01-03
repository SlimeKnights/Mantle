package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;

/** Command that opens a test book */
public class BookTestCommand {
  private static final BookData testBook = BookLoader.registerBook(Mantle.getResource("test"),
    new FileRepository(Mantle.getResource("books/test")));

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> source.getEntity() instanceof ServerPlayer)
      .executes(BookTestCommand::run);
  }

  /**
   * Runs the book-test command
   * @param context  Command context
   * @return  Integer return
   * @throws  CommandSyntaxException if sender is not a player
   */
  private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    MantleNetwork.INSTANCE.sendTo(new OpenNamedBookPacket(Mantle.getResource("test")), source.getPlayerOrException());
    return 0;
  }
}

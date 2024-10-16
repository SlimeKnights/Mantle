package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;

import javax.annotation.Nullable;

/** Command that clears the cache of a book or all books, faster than resource pack reloading for book writing */
public class ClearBookCacheCommand {
  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> source.getEntity() instanceof AbstractClientPlayer)
              .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleClientCommand.REGISTERED_BOOKS)
                            .executes(ClearBookCacheCommand::runBook))
              .executes(ClearBookCacheCommand::runAll);
  }

  /**
   * Runs the book-test command for specific book
   * @param context  Command context
   * @return  Integer return
   */
  private static int runBook(CommandContext<CommandSourceStack> context) {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");
    clearBookCache(book);
    return 0;
  }

  /**
   * Runs the book-test command
   * @param context  Command context
   * @return  Integer return
   */
  private static int runAll(CommandContext<CommandSourceStack> context) {
    clearBookCache(null);
    return 0;
  }

  private static void clearBookCache(@Nullable ResourceLocation book) {
    if (book != null) {
      BookData bookData = BookLoader.getBook(book);
      if (bookData != null) {
        bookData.reset();
      } else {
        OpenNamedBookPacket.ClientOnly.errorStatus(book);
      }
    } else {
      BookLoader.resetAllBooks();
    }
  }
}

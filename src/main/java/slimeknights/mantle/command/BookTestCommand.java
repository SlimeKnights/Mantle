package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;

import java.util.HashSet;
import java.util.Set;

/** Command that opens a test book */
public class BookTestCommand {
  private static final Set<ResourceLocation> BOOK_SUGGESTIONS = new HashSet<>();

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> source.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS) && source.getEntity() instanceof ServerPlayer)
      .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleCommand.REGISTERED_BOOKS)
        .executes(BookTestCommand::runBook))
      .executes(BookTestCommand::run);
  }

  /**
   * Adds a book suggestion to the list of suggestions
   * @param suggestion The suggestion to be added
   */
  public static void addBookSuggestion(ResourceLocation suggestion) {
    BOOK_SUGGESTIONS.add(suggestion);
  }

  /**
   * Gets all book suggestions
   * @return The suggestions as stream
   */
  public static Iterable<ResourceLocation> getBookSuggestions() {
    return BOOK_SUGGESTIONS;
  }

  /**
   * Runs the book-test command for specific book
   * @param context  Command context
   * @return  Integer return
   * @throws  CommandSyntaxException if sender is not a player
   */
  private static int runBook(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");

    CommandSourceStack source = context.getSource();
    MantleNetwork.INSTANCE.sendTo(new OpenNamedBookPacket(book), source.getPlayerOrException());
    return 0;
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

package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.ClearBookCachePacket;

/** Command that clears the cache of a book or all books, faster than resource pack reloading for book writing */
public class ClearBookCacheCommand {
  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> source.getEntity() instanceof ServerPlayer)
              .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(MantleCommand.REGISTERED_BOOKS)
                            .executes(ClearBookCacheCommand::runBook))
              .executes(ClearBookCacheCommand::runAll);
  }

  /**
   * Runs the book-test command for specific book
   * @param context  Command context
   * @return  Integer return
   * @throws CommandSyntaxException if sender is not a player
   */
  private static int runBook(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    ResourceLocation book = ResourceLocationArgument.getId(context, "id");
    MantleNetwork.INSTANCE.sendTo(new ClearBookCachePacket(book), context.getSource().getPlayerOrException());
    return 0;
  }

  /**
   * Runs the book-test command
   * @param context  Command context
   * @return  Integer return
   * @throws  CommandSyntaxException if sender is not a player
   */
  private static int runAll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    MantleNetwork.INSTANCE.sendTo(new ClearBookCachePacket((ResourceLocation)null), context.getSource().getPlayerOrException());
    return 0;
  }
}

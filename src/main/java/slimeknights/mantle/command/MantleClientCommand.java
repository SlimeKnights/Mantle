package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;

import java.util.function.Consumer;

/**
 * Root command for all commands in mantle
 */
public class MantleClientCommand {
  /** Suggestion provider that lists registered book ids **/
  public static SuggestionProvider<CommandSourceStack> REGISTERED_BOOKS;

  /** Registers all Mantle client command related content */
  public static void init() {
    // register arguments
    REGISTERED_BOOKS = SuggestionProviders.register(Mantle.getResource("registered_books"), (context, builder) ->
      SharedSuggestionProvider.suggestResource(BookLoader.getRegisteredBooks(), builder));

    // add command listener
    MinecraftForge.EVENT_BUS.addListener(MantleClientCommand::registerCommand);
  }

  /** Registers a sub command for the root Mantle client command */
  private static void register(LiteralArgumentBuilder<CommandSourceStack> root, String name, Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
    LiteralArgumentBuilder<CommandSourceStack> subCommand = Commands.literal(name);
    consumer.accept(subCommand);
    root.then(subCommand);
  }

  /** Event listener to register the Mantle command */
  private static void registerCommand(RegisterClientCommandsEvent event) {
    LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("mantle");

    // sub commands
    register(builder, "book", BookCommand::register);
    register(builder, "clear_book_cache", ClearBookCacheCommand::register);

    // register final command
    event.getDispatcher().register(builder);
  }
}

package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.command.client.BookCommand;

import java.util.HashSet;
import java.util.Set;

/**
 * Command that opens a test book
 * @deprecated Command is now client-side and lives in {@link BookCommand}
 */
public class BookTestCommand {
  private static final Set<ResourceLocation> BOOK_SUGGESTIONS = new HashSet<>();

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   * @deprecated Command is now client-side and lives in {@link BookCommand}
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {}

  /**
   * Adds a book suggestion to the list of suggestions
   * @param suggestion The suggestion to be added
   * @deprecated This method is no longer relevant due to the command becoming client-side
   */
  public static void addBookSuggestion(ResourceLocation suggestion) {
    BOOK_SUGGESTIONS.add(suggestion);
  }

  /**
   * Gets all book suggestions
   * @return The suggestions as stream
   * @deprecated Use {@link slimeknights.mantle.client.book.BookLoader#getRegisteredBooks()} instead
   */
  public static Iterable<ResourceLocation> getBookSuggestions() {
    return BOOK_SUGGESTIONS;
  }
}

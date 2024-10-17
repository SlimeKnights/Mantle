package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

/**
 * Command that clears the cache of a book or all books, faster than resource pack reloading for book writing
 * @deprecated Command is now client-side and lives in {@link slimeknights.mantle.command.client.ClearBookCacheCommand}
 */
public class ClearBookCacheCommand {
  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   * @deprecated Command is now client-side and lives in {@link slimeknights.mantle.command.client.ClearBookCacheCommand}
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {}
}

package slimeknights.mantle.command.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.command.SourcesCommand;
import slimeknights.mantle.command.SourcesCommand.SourceFolder;

import java.util.ArrayList;
import java.util.List;

/** Command to list all sources for a file in a resource pack */
public class ClientSourcesCommand {
  /** List of subcommands to add */
  private static final List<SourceFolder> FOLDERS = new ArrayList<>();

  /** Registers this command with the builder */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.then(Commands.literal("path")
      .then(Commands.argument("path", ResourceLocationArgument.id())
        .executes(context -> SourcesCommand.run(context, Minecraft.getInstance().getResourceManager(), ResourceLocationArgument.getId(context, "path")))));
    for (SourceFolder source : FOLDERS) {
      subCommand.then(Commands.literal(source.argument())
        .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(source.suggestionProvider())
          .executes(context -> run(context, source.folder(), ResourceLocationArgument.getId(context, "id"), source.extension()))));
    }
  }

  /** Runs for the given folder and extension */
  private static int run(CommandContext<CommandSourceStack> context, String folder, ResourceLocation id, String extension) throws CommandSyntaxException {
    return SourcesCommand.run(context, Minecraft.getInstance().getResourceManager(), id.withPath(folder + '/' + id.getPath() + extension));
  }


  /* Registering interesting folders */

  /** Suggests values using the passed suggestion provider */
  public static void register(String argument, String folder, String extension, SuggestionProvider<CommandSourceStack> suggestionProvider) {
    FOLDERS.add(new SourceFolder(argument, folder, extension, suggestionProvider));
  }

  public static void registerMinecraft(String folder, SuggestionProvider<CommandSourceStack> suggestionProvider) {
    register(folder, folder, ".json", suggestionProvider);
  }
}

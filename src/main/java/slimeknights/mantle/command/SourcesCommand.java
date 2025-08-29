package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import slimeknights.mantle.Mantle;

import java.util.ArrayList;
import java.util.List;

/** Command to list all sources for a file in a datapack */
public class SourcesCommand {
  /** Error on invalid item or tag ID */
  private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(id -> Mantle.makeComponent("command", "sources.not_found", id));
  /** List of subcommands to add */
  private static final List<SourceFolder> FOLDERS = new ArrayList<>();

  /** Registers this command with the builder */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand = subCommand.requires(source -> source.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN));
    subCommand.then(Commands.literal("path")
      .then(Commands.argument("path", ResourceLocationArgument.id())
        .executes(context -> run(context, context.getSource().getServer().getResourceManager(), ResourceLocationArgument.getId(context, "path")))));
    for (SourceFolder source : FOLDERS) {
      subCommand.then(Commands.literal(source.argument)
        .then(Commands.argument("id", ResourceLocationArgument.id()).suggests(source.suggestionProvider)
          .executes(context -> run(context, source.folder, ResourceLocationArgument.getId(context, "id"), source.extension))));
    }
  }

  /** Runs for the given folder and extension */
  private static int run(CommandContext<CommandSourceStack> context, String folder, ResourceLocation id, String extension) throws CommandSyntaxException {
    return run(context, context.getSource().getServer().getResourceManager(), id.withPath(folder + '/' + id.getPath() + extension));
  }

  /** Runs for the given folder and extension */
  public static int run(CommandContext<CommandSourceStack> context, ResourceManager manager, String folder, ResourceLocation id, String extension) throws CommandSyntaxException {
    return run(context, manager, id.withPath(folder + '/' + id.getPath() + extension));
  }

  /** Runs for the given ID and resource manager */
  public static int run(CommandContext<CommandSourceStack> context, ResourceManager manager, ResourceLocation path) throws CommandSyntaxException {
    List<String> packs = manager.getResourceStack(path).stream().map(Resource::sourcePackId).toList();
    if (packs.isEmpty()) {
      throw NOT_FOUND.create(path);
    }
    // print all the packs its found in
    context.getSource().sendSuccess(() -> {
      MutableComponent component = Component.translatable("command.mantle.sources.success", path);
      for (String pack : packs) {
        component = component.append(Component.literal("\n* " + (pack.isEmpty() ? "<unnamed>" : pack)));
      }
      return component;
    }, true);
    return packs.size();
  }


  /* Registering interesting folders */

  /** Creates a suggestion provider that suggests any value in a data pack folder */
  public static SuggestionProvider<CommandSourceStack> suggestFolder(FileToIdConverter converter) {
    return (context, builder) -> SharedSuggestionProvider.suggestResource(
      converter.listMatchingResources(context.getSource().getServer().getResourceManager()).keySet().stream().map(converter::fileToId), builder);
  }

  /** Helper to allow adding custom folders to the sources command */
  public record SourceFolder(String argument, String folder, String extension, SuggestionProvider<CommandSourceStack> suggestionProvider) {}

  /** Suggests value for the given folder */
  public static void register(String argument, String folder, String extension, SuggestionProvider<CommandSourceStack> suggestionProvider) {
    FOLDERS.add(new SourceFolder(argument, folder, extension, suggestionProvider));
  }

  /** Suggests values using the passed suggestion provider */
  public static void register(String folder, SuggestionProvider<CommandSourceStack> suggestionProvider) {
    register(folder, folder, ".json", suggestionProvider);
  }
}

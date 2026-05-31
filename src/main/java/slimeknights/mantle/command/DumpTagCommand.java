package slimeknights.mantle.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagLoader.EntryWithSource;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.command.argument.TagSource;
import slimeknights.mantle.command.argument.TagSourceArgument;
import slimeknights.mantle.util.JsonHelper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Command that dumps a tag into a JSON object.
 * TODO 1.21: rename to {@code TagEntriesCommand}.
 * TODO 1.21: move to {@link slimeknights.mantle.command.tags}.
 */
public class DumpTagCommand {
  protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
      .then(Action.LOG.build())
      .then(Action.SAVE.build())
      .then(Action.SOURCES.build());
  }

  private enum Action {
    SAVE, LOG, SOURCES;

    /** Builds the command for this action */
    public ArgumentBuilder<CommandSourceStack, ?> build() {
      return Commands.literal(name().toLowerCase())
        .then(TagSourceArgument.argument().then(TagSourceArgument.tagArgument("name")
          .executes(context -> run(context, this))));
    }
  }

  /**
   * Runs the view-tag command
   *
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static int run(CommandContext<CommandSourceStack> context, Action action) throws CommandSyntaxException {
    return runGeneric(context, TagSourceArgument.get(context), action);
  }

  /** Parses a tag from the resource list */
  public static void parseTag(List<Resource> resources, List<TagLoader.EntryWithSource> list, ResourceLocation regName, ResourceLocation tagName, ResourceLocation path) {
    for (Resource resource : resources) {
      String packId = resource.sourcePackId();
      try (Reader reader = resource.openAsReader()) {
        TagFile tagfile = JsonHelper.parse(TagFile.CODEC, reader);
        if (tagfile.replace()) {
          list.clear();
        }
        tagfile.entries().forEach(tag -> list.add(new TagLoader.EntryWithSource(tag, packId)));
        tagfile.remove().forEach(tag -> list.add(new TagLoader.EntryWithSource(tag, packId, true)));
      } catch (RuntimeException | IOException ex) {
        // failed to parse
        Mantle.logger.error("Couldn't read {} tag list {} from {} in data pack {}", regName, tagName, path, packId, ex);
      }
    }
  }

  /** Converts the given entry list to a string tag file */
  public static String tagToJson(List<TagLoader.EntryWithSource> entries) {
    return GSON.toJson(JsonHelper.serialize(TagFile.CODEC, new TagFile(
      // TODO: cancel out matching entries?
      entries.stream().filter(e -> !e.remove()).map(EntryWithSource::entry).toList(),
      true,
      entries.stream().filter(EntryWithSource::remove).map(EntryWithSource::entry).toList()
    )));
  }

  /** Saves the tag to the given path */
  public static void saveTag(List<TagLoader.EntryWithSource> entries, Path path) {
    try {
      Files.createDirectories(path.getParent());
      try (BufferedWriter writer = Files.newBufferedWriter(path)) {
        writer.write(tagToJson(entries));
      }
    } catch (IOException ex) {
      Mantle.logger.error("Couldn't save tag to {}", path, ex);
    }
  }

  /**
   * Runs the view-tag command, with the generic for the registry so those don't get mad
   *
   * @param context   Tag context
   * @param registry  Registry
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static <T> int runGeneric(CommandContext<CommandSourceStack> context, TagSource<T> registry, Action action) throws CommandSyntaxException {
    ResourceLocation regName = registry.key().location();
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);
    ResourceManager manager = context.getSource().getServer().getResourceManager();

    ResourceLocation path = ResourceLocation.fromNamespaceAndPath(name.getNamespace(), registry.folder() + "/" + name.getPath() + ".json");

    // if the tag file does not exist, only error if the tag is unknown
    List<Resource> resources = manager.getResourceStack(path);
    // if the tag does not exist in the collection, probably an invalid tag name
    if (resources.isEmpty() && !registry.hasTag(name)) {
      throw ViewTagCommand.TAG_NOT_FOUND.create(regName, name);
    }

    // simply create a tag builder
    List<TagLoader.EntryWithSource> list = new ArrayList<>();
    parseTag(resources, list, regName, name, path);

    // builder done, ready to dump
    // if requested, save
    switch (action) {
      case SAVE -> {
        // save creates a file in the data dump location of the tag at the proper path
        Path output = DumpAllTagsCommand.getOutputFile(context).toPath().resolve(path.getNamespace() + "/" + path.getPath());
        saveTag(list, output);
        context.getSource().sendSuccess(() -> Component.translatable("command.mantle.dump_tag.success_log", regName, name, GeneratePackHelper.getOutputComponent(output)), true);
      }
      case LOG -> {
        // log writes the merged JSON to the console
        Component message = Component.translatable("command.mantle.dump_tag.success", regName, name);
        context.getSource().sendSuccess(() -> message, true);
        Mantle.logger.info("Tag dump of {} tag '{}':\n{}", regName, name, tagToJson(list));
      }
      case SOURCES -> {
        // sources prints a list of each entry and the source of the entry
        Component message = Component.translatable("command.mantle.dump_tag.success", regName, name);
        context.getSource().sendSuccess(() -> message, true);
        StringBuilder builder = new StringBuilder();
        builder.append("Tag list dump of ").append(regName).append(" tag ").append(name).append(" with sources:");
        for (TagLoader.EntryWithSource entry : list) {
          if (entry.remove()) {
            builder.append("\n- '");
          } else {
            builder.append("\n+ '");
          }
          builder.append(entry.entry()).append("' from '").append(entry.source()).append('\'');
        }
        Mantle.logger.info(builder.toString());
      }
    }
    return resources.size();
  }
}

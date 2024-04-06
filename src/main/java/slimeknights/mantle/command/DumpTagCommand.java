package slimeknights.mantle.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagLoader.EntryWithSource;
import net.minecraft.tags.TagManager;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.Mantle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Command that dumps a tag into a JSON object */
public class DumpTagCommand {
  protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Dynamic2CommandExceptionType ERROR_READING_TAG = new Dynamic2CommandExceptionType((type, name) -> Component.translatable("command.mantle.dump_tag.read_error", type, name));
  private static final Component SUCCESS_LOG = Component.translatable("command.mantle.dump_tag.success_log");

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
              .then(Commands.argument("type", RegistryArgument.registry()).suggests(MantleCommand.REGISTRY)
                            .then(Commands.argument("name", ResourceLocationArgument.id()).suggests(MantleCommand.VALID_TAGS)
                                          .executes(context -> run(context, Action.LOG))
                                          .then(Commands.literal("log").executes(context -> run(context, Action.LOG)))
                                          .then(Commands.literal("save").executes(context -> run(context, Action.SAVE)))
                                          .then(Commands.literal("sources").executes(context -> run(context, Action.SOURCES)))));
  }

  /**
   * Runs the view-tag command
   *
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static int run(CommandContext<CommandSourceStack> context, Action action) throws CommandSyntaxException {
    return runGeneric(context, RegistryArgument.getResult(context, "type"), action);
  }

  /** Parses a tag from the resource list */
  public static void parseTag(List<Resource> resources, List<TagLoader.EntryWithSource> list, ResourceLocation regName, ResourceLocation tagName, ResourceLocation path) {
    for (Resource resource : resources) {
      String packId = resource.sourcePackId();
      try (Reader reader = resource.openAsReader()) {
        JsonObject json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
        TagFile tagfile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, json)).getOrThrow(false, Mantle.logger::error);
        if (tagfile.replace()) {
          list.clear();
        }
        tagfile.entries().forEach(tag -> list.add(new TagLoader.EntryWithSource(tag, packId)));
      } catch (RuntimeException | IOException ex) {
        // failed to parse
        Mantle.logger.error("Couldn't read {} tag list {} from {} in data pack {}", regName, tagName, path, packId, ex);
      }
    }
  }

  /** Converts the given entry list to a string tag file */
  public static String tagToJson(List<TagLoader.EntryWithSource> entries) {
    return GSON.toJson(
      TagFile.CODEC.encodeStart(
        JsonOps.INSTANCE,
        new TagFile(entries.stream().map(EntryWithSource::entry).toList(), true)
      ).getOrThrow(false, Mantle.logger::error));
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

  private enum Action { SAVE, LOG, SOURCES }

  /**
   * Runs the view-tag command, with the generic for the registry so those don't get mad
   *
   * @param context   Tag context
   * @param registry  Registry
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static <T> int runGeneric(CommandContext<CommandSourceStack> context, Registry<T> registry, Action action) throws CommandSyntaxException {
    ResourceLocation regName = registry.key().location();
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);
    ResourceManager manager = context.getSource().getServer().getResourceManager();

    ResourceLocation path = new ResourceLocation(name.getNamespace(), TagManager.getTagDir(registry.key()) + "/" + name.getPath() + ".json");

    // if the tag file does not exist, only error if the tag is unknown
    List<Resource> resources = manager.getResourceStack(path);
    // if the tag does not exist in the collection, probably an invalid tag name
    if (resources.isEmpty() && registry.getTag(TagKey.create(registry.key(), name)).isEmpty()) {
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
        File output = new File(DumpAllTagsCommand.getOutputFile(context), path.getNamespace() + "/" + path.getPath());
        saveTag(list, output.toPath());
        context.getSource().sendSuccess(() -> Component.translatable("command.mantle.dump_tag.success_log", regName, name, DumpAllTagsCommand.getOutputComponent(output)), true);
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
          builder.append("\n* '").append(entry.entry()).append("' from '").append(entry.source()).append('\'');
        }
        Mantle.logger.info(builder.toString());
      }
    }
    return resources.size();
  }
}

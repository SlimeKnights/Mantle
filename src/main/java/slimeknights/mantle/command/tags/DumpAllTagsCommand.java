package slimeknights.mantle.command.tags;

import com.google.common.collect.Maps;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import slimeknights.mantle.command.GeneratePackHelper;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.command.argument.TagSource;
import slimeknights.mantle.command.argument.TagSourceArgument;
import slimeknights.mantle.util.JsonHelper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Dumps all tags to a folder. */
public class DumpAllTagsCommand {

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
              .executes(DumpAllTagsCommand::runAll)
              .then(TagSourceArgument.argument().executes(DumpAllTagsCommand::runType));
  }

  /** Dumps all tags to the game directory */
  private static int runAll(CommandContext<CommandSourceStack> context) {
    File output = GeneratePackHelper.getDataDumpFile(context);
    int tagsDumped = TagSourceArgument.allSources(context).mapToInt(reg -> runForFolder(context, reg, output)).sum();
    // print the output path
    context.getSource().sendSuccess(() -> Component.translatable("command.mantle.dump_all_tags.success", GeneratePackHelper.getOutputComponent(output)), true);
    return tagsDumped;
  }

  /** Dumps a single type of tags to the game directory */
  private static int runType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    File output = GeneratePackHelper.getDataDumpFile(context);
    TagSource<?> registry = TagSourceArgument.get(context);
    int result = runForFolder(context, registry, output);
    // print result
    context.getSource().sendSuccess(() -> Component.translatable("command.mantle.dump_all_tags.type_success", registry.key().location(), GeneratePackHelper.getOutputComponent(output)), true);
    return result;
  }

  /**
   * Runs the view-tag command
   * @param context  Tag context
   * @return  Integer return
   */
  private static int runForFolder(CommandContext<CommandSourceStack> context, TagSource<?> registry, File output) {
    Map<ResourceLocation,List<TagLoader.EntryWithSource>> foundTags = Maps.newHashMap();
    MinecraftServer server = context.getSource().getServer();
    ResourceManager manager = server.getResourceManager();
    ResourceLocation tagType = registry.key().location();

    // iterate all tags from the datapack
    String dataPackFolder = registry.folder();
    for (Map.Entry<ResourceLocation,List<Resource>> entry : manager.listResourceStacks(dataPackFolder, fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
      ResourceLocation resourcePath = entry.getKey();
      ResourceLocation tagId = JsonHelper.localize(resourcePath, dataPackFolder, ".json");
      TagEntriesCommand.parseTag(entry.getValue(), foundTags.computeIfAbsent(resourcePath, id -> new ArrayList<>()), tagType, tagId, resourcePath);
    }

    // save all tags
    for (Entry<ResourceLocation, List<TagLoader.EntryWithSource>> entry : foundTags.entrySet()) {
      ResourceLocation location = entry.getKey();
      Path path = output.toPath().resolve(location.getNamespace() + "/" + location.getPath());
      TagEntriesCommand.saveTag(entry.getValue(), path);
    }

    return foundTags.size();
  }
}

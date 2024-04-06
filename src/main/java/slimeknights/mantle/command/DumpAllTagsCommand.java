package slimeknights.mantle.command;

import com.google.common.collect.Maps;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagManager;
import slimeknights.mantle.util.JsonHelper;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Dumps all tags to a folder
 */
public class DumpAllTagsCommand {
  private static final String TAG_DUMP_PATH = "./mantle_data_dump";

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
              .executes(DumpAllTagsCommand::runAll)
              .then(Commands.argument("type", RegistryArgument.registry()).suggests(MantleCommand.REGISTRY)
                            .executes(DumpAllTagsCommand::runType));
  }

  /** Gets the path for the output */
  protected static File getOutputFile(CommandContext<CommandSourceStack> context) {
    return context.getSource().getServer().getFile(TAG_DUMP_PATH);
  }

  /**
   * Makes a clickable text component for the output folder
   * @param file  File
   * @return  Clickable text component
   */
  protected static Component getOutputComponent(File file) {
    return Component.literal(file.getAbsolutePath()).withStyle(style -> style.withUnderlined(true).withClickEvent(new ClickEvent(Action.OPEN_FILE, file.getAbsolutePath())));
  }

  /** Dumps all tags to the game directory */
  private static int runAll(CommandContext<CommandSourceStack> context) {
    File output = getOutputFile(context);
    int tagsDumped = context.getSource().registryAccess().registries().mapToInt(r -> runForFolder(context, r.key(), output)).sum();
    // print the output path
    context.getSource().sendSuccess(() -> Component.translatable("command.mantle.dump_all_tags.success", getOutputComponent(output)), true);
    return tagsDumped;
  }

  /** Dumps a single type of tags to the game directory */
  private static int runType(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    File output = getOutputFile(context);
    Registry<?> registry = RegistryArgument.getResult(context, "type");
    int result = runForFolder(context, registry.key(), output);
    // print result
    context.getSource().sendSuccess(() -> Component.translatable("command.mantle.dump_all_tags.type_success", registry.key().location(), getOutputComponent(output)), true);
    return result;
  }

  /**
   * Runs the view-tag command
   * @param context  Tag context
   * @return  Integer return
   */
  private static int runForFolder(CommandContext<CommandSourceStack> context, ResourceKey<? extends Registry<?>> key, File output) {
    Map<ResourceLocation,List<TagLoader.EntryWithSource>> foundTags = Maps.newHashMap();
    MinecraftServer server = context.getSource().getServer();
    ResourceManager manager = server.getResourceManager();
    ResourceLocation tagType = key.location();

    // iterate all tags from the datapack
    String dataPackFolder = TagManager.getTagDir(key);
    for (Map.Entry<ResourceLocation,List<Resource>> entry : manager.listResourceStacks(dataPackFolder, fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
      ResourceLocation resourcePath = entry.getKey();
      ResourceLocation tagId = JsonHelper.localize(resourcePath, dataPackFolder, ".json");
      DumpTagCommand.parseTag(entry.getValue(), foundTags.computeIfAbsent(resourcePath, id -> new ArrayList<>()), tagType, tagId, resourcePath);
    }

    // save all tags
    for (Entry<ResourceLocation, List<TagLoader.EntryWithSource>> entry : foundTags.entrySet()) {
      ResourceLocation location = entry.getKey();
      Path path = output.toPath().resolve(location.getNamespace() + "/" + location.getPath());
      // TODO: is it worth including the sources anywhere in the dump?
      DumpTagCommand.saveTag(entry.getValue(), path);
    }

    return foundTags.size();
  }
}

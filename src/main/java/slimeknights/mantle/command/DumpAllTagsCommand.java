package slimeknights.mantle.command;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ITag.Builder;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.io.IOUtils;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.command.TagCollectionArgument.VanillaTagType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import static slimeknights.mantle.command.DumpTagCommand.GSON;

/**
 * Dumps all tags to a folder
 */
public class DumpAllTagsCommand {
  private static final String TAG_DUMP_PATH = "./mantle_data_dump";
  private static final int EXTENSION_LENGTH = ".json".length();

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSource> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
              .executes(DumpAllTagsCommand::runAll)
              .then(Commands.argument("type", TagCollectionArgument.collection())
                            .executes(DumpAllTagsCommand::runType));
  }

  /** Gets the path for the output */
  protected static File getOutputFile(CommandContext<CommandSource> context) {
    return context.getSource().getServer().getFile(TAG_DUMP_PATH);
  }

  /**
   * Makes a clickable text component for the output folder
   * @param file  File
   * @return  Clickable text component
   */
  protected static ITextComponent getOutputComponent(File file) {
    return new StringTextComponent(file.getAbsolutePath()).withStyle(style -> style.setUnderlined(true).withClickEvent(new ClickEvent(Action.OPEN_FILE, file.getAbsolutePath())));
  }

  /** Dumps all tags to the game directory */
  private static int runAll(CommandContext<CommandSource> context) throws CommandSyntaxException {
    File output = getOutputFile(context);
    int tagsDumped = 0;
    for (VanillaTagType type : VanillaTagType.values()) {
      tagsDumped += runForFolder(context, type.getName(), type.getTagFolder(), output);
    }

    for (ResourceLocation type : ForgeTagHandler.getCustomTagTypeNames()) {
      ForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(type);
      if (registry != null && registry.getTagFolder() != null) {
        tagsDumped += runForFolder(context, type, registry.getTagFolder(), output);
      }
    }
    // print the output path
    context.getSource().sendSuccess(new TranslationTextComponent("command.mantle.dump_all_tags.success", getOutputComponent(output)), true);
    return tagsDumped;
  }

  /** Dumps a single type of tags to the game directory */
  private static int runType(CommandContext<CommandSource> context) throws CommandSyntaxException {
    File output = getOutputFile(context);
    TagCollectionArgument.Result type = context.getArgument("type", TagCollectionArgument.Result.class);
    int result = runForFolder(context, type.getName(), type.getTagFolder(), output);
    // print result
    context.getSource().sendSuccess(new TranslationTextComponent("command.mantle.dump_all_tags.type_success", type.getName(), getOutputComponent(output)), true);
    return result;
  }

  /**
   * Runs the view-tag command
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static int runForFolder(CommandContext<CommandSource> context, ResourceLocation tagType, String tagFolder, File output) throws CommandSyntaxException {
    Map<ResourceLocation, Builder> foundTags = Maps.newHashMap();
    MinecraftServer server = context.getSource().getServer();
    IResourceManager manager = server.getDataPackRegistries().getResourceManager();

    // iterate all tags from the datapack
    String dataPackFolder = "tags/" + tagFolder;
    for (ResourceLocation resourcePath : manager.listResources(dataPackFolder, fileName -> fileName.endsWith(".json"))) {
      String path = resourcePath.getPath();
      ResourceLocation tagId = new ResourceLocation(resourcePath.getNamespace(), path.substring(dataPackFolder.length() + 1, path.length() - EXTENSION_LENGTH));
      try {
        for (IResource resource : manager.getResources(resourcePath)) {
          try (
            InputStream inputstream = resource.getInputStream();
            Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
          ) {
            JsonObject json = JSONUtils.fromJson(GSON, reader, JsonObject.class);
            if (json == null) {
              Mantle.logger.error("Couldn't load {} tag list {} from {} in data pack {} as it is empty or null", tagType, tagId, resourcePath, resource.getSourceName());
            } else {
              // store by the resource path instead of the ID, thats the one we want at the end
              foundTags.computeIfAbsent(resourcePath, id -> Builder.tag()).addFromJson(json, resource.getSourceName());
            }
          } catch (RuntimeException | IOException ex) {
            Mantle.logger.error("Couldn't read {} tag list {} from {} in data pack {}", tagType, tagId, resourcePath, resource.getSourceName(), ex);
          } finally {
            IOUtils.closeQuietly(resource);
          }
        }
      } catch (IOException ex) {
        Mantle.logger.error("Couldn't read {} tag list {} from {}", tagType, tagId, resourcePath, ex);
      }
    }

    // save all tags
    for (Entry<ResourceLocation, Builder> entry : foundTags.entrySet()) {
      ResourceLocation location = entry.getKey();
      Path path = output.toPath().resolve(location.getNamespace() + "/" + location.getPath());
      try {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
          writer.write(GSON.toJson(entry.getValue().serializeToJson()));
        }
      } catch (IOException ex) {
        Mantle.logger.error("Couldn't save tags to {}", path, ex);
      }
    }

    return foundTags.size();
  }
}

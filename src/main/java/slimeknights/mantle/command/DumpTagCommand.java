package slimeknights.mantle.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import slimeknights.mantle.Mantle;

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
import java.util.Collections;
import java.util.List;

/** Command that dumps a tag into a JSON object */
public class DumpTagCommand {
  protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Dynamic2CommandExceptionType ERROR_READING_TAG = new Dynamic2CommandExceptionType((type, name) -> new TranslatableComponent("command.mantle.dump_tag.read_error", type, name));
  private static final Component SUCCESS_LOG = new TranslatableComponent("command.mantle.dump_tag.success_log");

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
              .then(Commands.argument("type", TagCollectionArgument.collection())
                            .then(Commands.argument("name", ResourceLocationArgument.id()).suggests(MantleCommand.VALID_TAGS)
                                          .executes(context -> run(context, false))
                                          .then(Commands.literal("log").executes(context -> run(context, false)))
                                          .then(Commands.literal("save").executes(context -> run(context, true)))));
  }

  /**
   * Runs the view-tag command
   *
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static int run(CommandContext<CommandSourceStack> context, boolean saveFile) throws CommandSyntaxException {
    TagCollectionArgument.Result<?> type = TagCollectionArgument.getResult(context, "type");
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);
    ResourceManager manager = context.getSource().getServer().getResourceManager();

    ResourceLocation path = new ResourceLocation(name.getNamespace(), "tags/" + type.getTagFolder() + "/" + name.getPath() + ".json");

    // if the tag file does not exist, only error if the tag is unknown
    List<Resource> resources = Collections.emptyList();
    if (manager.hasResource(path)) {
      try {
        resources = manager.getResources(path);
      } catch (IOException ex) {
        // tag exists and we still could not read it? something went wrong
        Mantle.logger.error("Couldn't read {} tag list {} from {}", type.getName(), name, path, ex);
        throw ERROR_READING_TAG.create(type.getName(), name);
      }
    // if the tag does not exist in the collect, probably an invalid tag name
    } else if (type.getCollection().getTag(name) == null) {
      throw ViewTagCommand.TAG_NOT_FOUND.create(type.getName(), name);
    }

    // simply create a tag builder
    Tag.Builder builder = Tag.Builder.tag();
    int tagsProcessed = 0;
    for (Resource resource : resources) {
      try (
        InputStream inputstream = resource.getInputStream();
        Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8))
      ) {
        JsonObject json = GsonHelper.fromJson(GSON, reader, JsonObject.class);
        if (json == null) {
          // no json
          Mantle.logger.error("Couldn't load {} tag list {} from {} in data pack {} as it is empty or null", type.getName(), name, path, resource.getSourceName());
        } else {
          builder.addFromJson(json, resource.getSourceName());
          tagsProcessed++;
        }
      } catch (RuntimeException | IOException ex) {
        // failed to parse
        Mantle.logger.error("Couldn't read {} tag list {} from {} in data pack {}", type.getName(), name, path, resource.getSourceName(), ex);
      } finally {
        IOUtils.closeQuietly(resource);
      }
    }

    // builder done, ready to dump
    // if requested, save
    if (saveFile) {
      // save file
      File output = new File(DumpAllTagsCommand.getOutputFile(context), path.getNamespace() + "/" + path.getPath());
      Path outputPath = output.toPath();
      try {
        Files.createDirectories(outputPath.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
          writer.write(DumpTagCommand.GSON.toJson(builder.serializeToJson()));
        }
      } catch (IOException ex) {
        Mantle.logger.error("Couldn't save {} tag {} to {}", type.getName(), name, outputPath, ex);
      }
      context.getSource().sendSuccess(new TranslatableComponent("command.mantle.dump_tag.success_log", type.getName(), name, DumpAllTagsCommand.getOutputComponent(output)), true);
    } else {
      // print to console
      Component message = new TranslatableComponent("command.mantle.dump_tag.success", type.getName(), name);
      context.getSource().sendSuccess(message, true);
      Mantle.logger.info("Tag dump of {} tag '{}':\n{}", type.getName(), name, GSON.toJson(builder.serializeToJson()));
    }

    return tagsProcessed;
  }
}

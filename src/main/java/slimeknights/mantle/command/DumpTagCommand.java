package slimeknights.mantle.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.ITag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.io.IOUtils;
import slimeknights.mantle.Mantle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Command that dumps a tag into a JSON object */
public class DumpTagCommand {
  protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Dynamic2CommandExceptionType ERROR_READING_TAG = new Dynamic2CommandExceptionType((type, name) -> new TranslationTextComponent("command.mantle.dump_tag.read_error", type, name));

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSource> subCommand) {
    subCommand.requires(sender -> sender.hasPermissionLevel(MantleCommand.PERMISSION_EDIT_SPAWN))
              .then(Commands.argument("type", TagCollectionArgument.collection())
                            .then(Commands.argument("name", ResourceLocationArgument.resourceLocation()).suggests(MantleCommand.VALID_TAGS)
                                          .executes(DumpTagCommand::run)));
  }

  /**
   * Runs the view-tag command
   *
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
    TagCollectionArgument.Result type = context.getArgument("type", TagCollectionArgument.Result.class);
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);
    IResourceManager manager = context.getSource().getServer().getDataPackRegistries().getResourceManager();

    ResourceLocation path = new ResourceLocation(name.getNamespace(), "tags/" + type.getTagFolder() + "/" + name.getPath() + ".json");
    try {
      // fetch tags from all datapacks for this, logic mostly based on vanilla tag loader
      List<IResource> resources = manager.getAllResources(path);
      // simply create a tag builder
      ITag.Builder builder = ITag.Builder.create();
      int tagsProcessed = 0;
      for (IResource resource : resources) {
        try (
          InputStream inputstream = resource.getInputStream();
          Reader reader = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
        ) {
          JsonObject json = JSONUtils.fromJson(GSON, reader, JsonObject.class);
          if (json == null) {
            // no json
            Mantle.logger.error("Couldn't load {} tag list {} from {} in data pack {} as it is empty or null", type.getName(), name, path, resource.getPackName());
          } else {
            builder.deserialize(json, resource.getPackName());
            tagsProcessed++;
          }
        } catch (RuntimeException | IOException ex) {
          // failed to parse
          Mantle.logger.error("Couldn't read {} tag list {} from {} in data pack {}", type.getName(), name, path, resource.getPackName(), ex);
        } finally {
          IOUtils.closeQuietly(resource);
        }
      }

      // builder done, ready to dump
      ITextComponent message = new TranslationTextComponent("command.mantle.dump_tag.success", type.getName(), name);
      context.getSource().sendFeedback(message, true);
      Mantle.logger.info("Tag dump of {} tag '{}':\n{}", type.getName(), name, GSON.toJson(builder.serialize()));
      return tagsProcessed;
    } catch (IOException | RuntimeException ex) {
      // if the tag does not exist in the collect, probably an invalid tag name
      if (type.getCollection().get(name) == null) {
        throw ViewTagCommand.TAG_NOT_FOUND.create(type.getName(), name);
      } else {
        // tag exists and we still could not read it? something went wrong
        Mantle.logger.error("Couldn't read {} tag list {} from {}", type.getName(), name, path, ex);
        throw ERROR_READING_TAG.create(type.getName(), name);
      }
    }
  }
}

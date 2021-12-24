package slimeknights.mantle.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
import java.util.ArrayList;
import java.util.List;

/** Command to dump global loot modifiers */
public class DumpLootModifiers {
  /** Resource location of the global loot manager "tag" */
  protected static final ResourceLocation GLOBAL_LOOT_MODIFIERS = new ResourceLocation("forge", "loot_modifiers/global_loot_modifiers.json");
  /** Path for saving the loot modifiers */
  private static final String LOOT_MODIFIER_PATH = GLOBAL_LOOT_MODIFIERS.getNamespace() + "/" + GLOBAL_LOOT_MODIFIERS.getPath();

  // loot modifiers
  private static final ITextComponent LOOT_MODIFIER_SUCCESS_LOG = new TranslationTextComponent("command.mantle.dump_loot_modifiers.success_log");
  protected static final SimpleCommandExceptionType ERROR_READING_LOOT_MODIFIERS = new SimpleCommandExceptionType(new TranslationTextComponent("command.mantle.dump_loot_modifiers.read_error", GLOBAL_LOOT_MODIFIERS));

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSource> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
              .then(Commands.literal("save").executes(source -> run(source, true)))
              .then(Commands.literal("log").executes(source -> run(source, false)));
  }


  /** Runs the command, dumping the tag */
  private static int run(CommandContext<CommandSource> context, boolean saveFile) throws CommandSyntaxException {
    List<ResourceLocation> finalLocations = new ArrayList<>();
    IResourceManager manager = context.getSource().getServer().getDataPackRegistries().getResourceManager();
    try {
      // logic based on forge logic for reading loot managers
      for (IResource resource : manager.getResources(GLOBAL_LOOT_MODIFIERS)) {
        try (InputStream input = resource.getInputStream();
             Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
        ) {
          JsonObject json = JSONUtils.fromJson(DumpTagCommand.GSON, reader, JsonObject.class);
          if (json == null) {
            // no json
            Mantle.logger.error("Couldn't load global loot modifiers from {} in data pack {} as it is empty or null", GLOBAL_LOOT_MODIFIERS, resource.getSourceName());
          } else {
            // replace: remove all lower
            if (JSONUtils.getAsBoolean(json, "replace", false)) {
              finalLocations.clear();
            }
            JsonArray entryList = JSONUtils.getAsJsonArray(json, "entries");
            for (JsonElement entry : entryList) {
              ResourceLocation res = ResourceLocation.tryParse(JSONUtils.convertToString(entry, "entry"));
              if (res != null) {
                finalLocations.remove(res);
                finalLocations.add(res);
              }
            }
          }
        }
        catch (RuntimeException | IOException ex) {
          Mantle.logger.error("Couldn't read global loot modifier list {} in data pack {}", GLOBAL_LOOT_MODIFIERS, resource.getSourceName(), ex);
        } finally {
          IOUtils.closeQuietly(resource);
        }
      }
    } catch (IOException ex) {
      throw ERROR_READING_LOOT_MODIFIERS.create();
    }

    // save the list as JSON
    JsonArray entries = new JsonArray();
    for (ResourceLocation location : finalLocations) {
      entries.add(location.toString());
    }
    JsonObject json = new JsonObject();
    json.addProperty("replace", false);
    json.add("entries", entries);

    // if requested, save
    if (saveFile) {
      // save file
      File output = new File(DumpAllTagsCommand.getOutputFile(context), LOOT_MODIFIER_PATH);
      Path path = output.toPath();
      try {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
          writer.write(DumpTagCommand.GSON.toJson(json));
        }
      } catch (IOException ex) {
        Mantle.logger.error("Couldn't save global loot manager to {}", path, ex);
      }
      context.getSource().sendSuccess(new TranslationTextComponent("command.mantle.dump_loot_modifiers.success_save", DumpAllTagsCommand.getOutputComponent(output)), true);
    } else {
      // print to console
      context.getSource().sendSuccess(LOOT_MODIFIER_SUCCESS_LOG, true);
      Mantle.logger.info("Dump of global loot modifiers:\n{}", DumpTagCommand.GSON.toJson(json));
    }
    // return a number to finish
    return finalLocations.size();
  }
}

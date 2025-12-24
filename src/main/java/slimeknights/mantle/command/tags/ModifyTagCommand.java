package slimeknights.mantle.command.tags;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.command.GeneratePackHelper;
import slimeknights.mantle.command.MantleCommand;
import slimeknights.mantle.command.argument.ResourceOrTagKeyArgument;
import slimeknights.mantle.command.argument.TagSource;
import slimeknights.mantle.command.argument.TagSourceArgument;
import slimeknights.mantle.util.JsonHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/** Command to add or remove an entry from a tag */
public class ModifyTagCommand {
  private static final Dynamic2CommandExceptionType ERROR_READING_TAG = new Dynamic2CommandExceptionType((type, name) -> Component.translatable("command.mantle.modify_tag.read_error", type, name));
  private static final Dynamic2CommandExceptionType ERROR_WRITING_TAG = new Dynamic2CommandExceptionType((type, name) -> Component.translatable("command.mantle.modify_tag.write_error", type, name));

  /**
   * Registers this sub command with the root command
   * @param root  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
    root
      .then(Action.ADD.build())
      .then(Action.REMOVE.build())
      .then(Commands.literal("clear").requires(source -> MantleCommand.requiresDebugInfoOrOp(source, MantleCommand.PERMISSION_GAME_COMMANDS))
        .then(TagSourceArgument.argument().then(TagSourceArgument.tagArgument("tag")
          .executes(context -> clear(context, TagSourceArgument.get(context))))));
  }

  private enum Action {
    ADD,
    REMOVE;

    /** Builds a command for this action */
    public ArgumentBuilder<CommandSourceStack, ?> build() {
      return Commands.literal(name().toLowerCase(Locale.ROOT)).requires(source -> MantleCommand.requiresDebugInfoOrOp(source, MantleCommand.PERMISSION_GAME_COMMANDS))
        .then(TagSourceArgument.argument()
          .then(TagSourceArgument.tagArgument("tag")
            .then(TagSourceArgument.entryArgument("entry")
              .executes(context -> modify(context, TagSourceArgument.get(context), this)))));
    }
  }

  /** Saves the passed tag */
  private static void saveTag(ResourceLocation regName, ResourceLocation tag, Path path, TagFile contents) throws CommandSyntaxException {
    try {
      Files.createDirectories(path.getParent());
      try (BufferedWriter writer = Files.newBufferedWriter(path)) {
        writer.write(JsonHelper.DEFAULT_GSON.toJson(JsonHelper.serialize(TagFile.CODEC, contents)));
      }
    } catch (IOException ex) {
      Mantle.logger.error("Couldn't save {} tag {} to {}", regName, tag, path, ex);
      throw ERROR_WRITING_TAG.create(regName, tag);
    }
  }

  /** Gets the path for the given tag */
  private static Path getPath(Path pack, TagSource<?> registry, ResourceLocation tag) {
    return pack.resolve(PackType.SERVER_DATA.getDirectory() + '/' + tag.getNamespace() + '/' + registry.folder() + '/' + tag.getPath() + ".json");
  }

  /** Gets the clickable component for the given tag */
  private static Component tagComponent(ResourceLocation tag, Path path) {
    return GeneratePackHelper.getPathComponent(Component.literal(tag.toString()), path.toString());
  }

  /** Runs the command */
  private static <T> int modify(CommandContext<CommandSourceStack> context, TagSource<T> registry, Action action) throws CommandSyntaxException {
    ResourceLocation regName = registry.key().location();
    ResourceLocation tag = context.getArgument("tag", ResourceLocation.class);
    ResourceOrTagKeyArgument.Result entry = ResourceOrTagKeyArgument.get(context, "entry");

    // setup the pack
    CommandSourceStack source = context.getSource();
    Path pack = GeneratePackHelper.getDatapackPath(source.getServer());
    GeneratePackHelper.saveMcmeta(pack);

    // fetch existing tag, if it exists
    Path tagPath = getPath(pack, registry, tag);

    // load in existing tag from the path, not using resource managers as we are just modifying locally
    List<TagEntry> add = new ArrayList<>();
    List<TagEntry> remove = new ArrayList<>();
    boolean replace = false;
    if (Files.exists(tagPath)) {
      try (BufferedReader reader = Files.newBufferedReader(tagPath)) {
        TagFile tagfile = JsonHelper.parse(TagFile.CODEC, reader);
        add.addAll(tagfile.entries());
        remove.addAll(tagfile.remove());
        replace = tagfile.replace();
      } catch (Exception e) {
        Mantle.logger.error("Failed to load {} tag {} from {}", regName, tag, tagPath, e);
        throw ERROR_READING_TAG.create(regName, tag);
      }
    }

    // add the new entry
    TagEntry tagEntry = entry.tagEntry();
    int changed = 0;
    if (action == Action.ADD) {
      // ensure the entry is not being removed
      if (remove(remove, tagEntry)) {
        changed += 1;
      }
      if (add(add, tagEntry)) {
        changed += 1;
      }
    } else {
      // ensure the entry is not being added
      if (remove(add, tagEntry)) {
        changed += 1;
      }
      // remove it if not removed
      if (add(remove, tagEntry)) {
        changed += 1;
      }
    }

    // save the new tag
    if (changed > 0) {
      saveTag(regName, tag, tagPath, new TagFile(add, replace, remove));
    }

    // success
    source.sendSuccess(() -> Component.translatable(
      "command.mantle.modify_tag.success." + action.name().toLowerCase(Locale.ROOT),
      entry, regName, tagComponent(tag, tagPath), GeneratePackHelper.getOutputComponent(pack)), true);
    return changed;
  }

  /** Runs the command */
  private static <T> int clear(CommandContext<CommandSourceStack> context, TagSource<T> registry) throws CommandSyntaxException {
    ResourceLocation regName = registry.key().location();
    ResourceLocation tag = context.getArgument("tag", ResourceLocation.class);

    // setup the pack
    CommandSourceStack source = context.getSource();
    Path pack = GeneratePackHelper.getDatapackPath(source.getServer());
    GeneratePackHelper.saveMcmeta(pack);

    // fetch existing tag, if it exists
    Path tagPath = getPath(pack, registry, tag);

    // add an empty tag at the target
    saveTag(regName, tag, tagPath, new TagFile(List.of(), true, List.of()));

    // success
    source.sendSuccess(() -> Component.translatable("command.mantle.modify_tag.success.clear", regName, tagComponent(tag, tagPath), GeneratePackHelper.getOutputComponent(pack)), true);
    return 0;
  }


  /* Tag helpers */

  /** Checks if two entries are equal */
  private static boolean equals(TagEntry left, TagEntry right) {
    return left.isTag() == right.isTag() && left.isRequired() == right.isRequired() && left.getId().equals(right.getId());
  }

  /** Removes the entry from the list */
  private static boolean remove(List<TagEntry> entries, TagEntry toRemove) {
    Iterator<TagEntry> iterator = entries.iterator();
    while (iterator.hasNext()) {
      TagEntry entry = iterator.next();
      if (equals(entry, toRemove)) {
        iterator.remove();
        return true;
      }
    }
    return false;
  }

  /** Adds the entry to the list, skipping if already presnet */
  private static boolean add(List<TagEntry> entries, TagEntry toAdd) {
    for (TagEntry entry : entries) {
      if (equals(entry, toAdd)) {
        return false;
      }
    }
    entries.add(toAdd);
    return true;
  }
}

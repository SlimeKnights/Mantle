package slimeknights.mantle.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;
import slimeknights.mantle.Mantle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Command to dump global loot modifiers */
public class HarvestTiersCommand {
  /** Resource location of the global loot manager "tag" */
  protected static final ResourceLocation HARVEST_TIERS = new ResourceLocation("forge", "item_tier_ordering.json");
  /** Path for saving the loot modifiers */
  private static final String HARVEST_TIER_PATH = HARVEST_TIERS.getNamespace() + "/" + HARVEST_TIERS.getPath();

  // loot modifiers
  private static final Component SUCCESS_LOG = new TranslatableComponent("command.mantle.harvest_tiers.success_log");
  private static final Component EMPTY = new TranslatableComponent("command.mantle.tag.empty");

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
              .then(Commands.literal("save").executes(source -> run(source, true)))
              .then(Commands.literal("log").executes(source -> run(source, false)))
              .then(Commands.literal("list").executes(HarvestTiersCommand::list));
  }

  /** Creates a clickable component for a block tag */
  private static Object getTagComponent(TagCollection<Block> blockTags, Tag<Block> tag) {
    ResourceLocation id = blockTags.getId(tag);
    if (id == null) {
      return "";
    }
    return new TextComponent(id.toString()).withStyle(style -> style.setUnderlined(true).withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/mantle dump_tag " + Registry.BLOCK_REGISTRY.location() + " " + id + " save")));
  }

  /** Runs the command, dumping the tag */
  private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    List<Tier> sortedTiers = TierSortingRegistry.getSortedTiers();

    // start building output message
    MutableComponent output = new TranslatableComponent("command.mantle.harvest_tiers.success_list");
    // if no values, print empty
    if (sortedTiers.isEmpty()) {
      output.append("\n* ").append(EMPTY);
    } else {
      TagCollection<Block> blockTags = SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY);
      for (Tier tier : sortedTiers) {
        output.append("\n* ");
        Tag<Block> tag = tier.getTag();
        ResourceLocation id = TierSortingRegistry.getName(tier);
        if (tag != null) {
          output.append(new TranslatableComponent("command.mantle.harvest_tiers.tag", id, getTagComponent(blockTags, tag)));
        } else {
          output.append(new TranslatableComponent("command.mantle.harvest_tiers.no_tag", id));
        }
      }
    }
    context.getSource().sendSuccess(output, true);
    return sortedTiers.size();
  }

  /** Runs the command, dumping the tag */
  private static int run(CommandContext<CommandSourceStack> context, boolean saveFile) throws CommandSyntaxException {
    List<Tier> sortedTiers = TierSortingRegistry.getSortedTiers();

    // save the list as JSON
    JsonArray entries = new JsonArray();
    for (Tier location : sortedTiers) {
      entries.add(Objects.requireNonNull(TierSortingRegistry.getName(location)).toString());
    }
    JsonObject json = new JsonObject();
    json.add("order", entries);

    // if requested, save
    if (saveFile) {
      // save file
      File output = new File(DumpAllTagsCommand.getOutputFile(context), HARVEST_TIER_PATH);
      Path path = output.toPath();
      try {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
          writer.write(DumpTagCommand.GSON.toJson(json));
        }
      } catch (IOException ex) {
        Mantle.logger.error("Couldn't save harvests tiers to {}", path, ex);
      }
      context.getSource().sendSuccess(new TranslatableComponent("command.mantle.harvest_tiers.success_save", DumpAllTagsCommand.getOutputComponent(output)), true);
    } else {
      // print to console
      context.getSource().sendSuccess(SUCCESS_LOG, true);
      Mantle.logger.info("Dump of harvests tiers:\n{}", DumpTagCommand.GSON.toJson(json));
    }
    // return a number to finish
    return sortedTiers.size();
  }
}

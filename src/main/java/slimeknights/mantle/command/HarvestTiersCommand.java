package slimeknights.mantle.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.CommonHooks;
import slimeknights.mantle.Mantle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.List;
import java.util.Set;

/** Command to dump global loot modifiers */
public class HarvestTiersCommand {
  /** Resource location of the global loot manager "tag" */
  protected static final ResourceLocation HARVEST_TIERS = ResourceLocation.fromNamespaceAndPath(Mantle.modId, "item_tiers.json");
  /** Path for saving the loot modifiers */
  private static final String HARVEST_TIER_PATH = HARVEST_TIERS.getNamespace() + "/" + HARVEST_TIERS.getPath();

  // loot modifiers
  private static final Component SUCCESS_LOG = Component.translatable("command.mantle.harvest_tiers.success_log");
  private static final Component EMPTY = Component.translatable("command.mantle.tag.empty");

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
  private static Object getTagComponent(TagKey<Block> tag) {
    ResourceLocation id = tag.location();
    return Component.literal(id.toString()).withStyle(style -> style.withUnderlined(true).withClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, "/mantle dump_tag " + Registries.BLOCK.location() + " " + id + " save")));
  }

  /** Best-effort tier ID for command output and JSON export */
  private static ResourceLocation getTierId(Tier tier) {
    if (tier instanceof Tiers vanilla) {
      return ResourceLocation.withDefaultNamespace(vanilla.name().toLowerCase(Locale.ROOT));
    }

    ResourceLocation incorrectTag = tier.getIncorrectBlocksForDrops().location();
    String path = incorrectTag.getPath();
    if (path.startsWith("incorrect_for_") && path.endsWith("_tool")) {
      path = path.substring("incorrect_for_".length(), path.length() - "_tool".length());
      if (path.equals("wooden")) {
        path = "wood";
      }
    }
    return ResourceLocation.fromNamespaceAndPath(incorrectTag.getNamespace(), path);
  }

  /** Gets the block tag to display for the given tier */
  private static TagKey<Block> getTierTag(Tier tier) {
    if (tier instanceof Tiers vanilla) {
      return CommonHooks.getTagFromVanillaTier(vanilla);
    }
    return tier.getIncorrectBlocksForDrops();
  }

  /** Gets the count of blocks in the incorrect-for-drops tag for sorting */
  private static int getIncorrectBlocksCount(Tier tier) {
    return BuiltInRegistries.BLOCK.getTag(tier.getIncorrectBlocksForDrops()).map(HolderSet::size).orElse(0);
  }

  /** Gets tiers in best-effort sorted order, including modded tiers found on {@link TieredItem}s */
  private static List<Tier> getSortedTiers() {
    Set<Tier> tiers = new LinkedHashSet<>();
    tiers.addAll(List.of(Tiers.values()));
    for (Item item : BuiltInRegistries.ITEM) {
      if (item instanceof TieredItem tiered) {
        tiers.add(tiered.getTier());
      }
    }

    List<Tier> sorted = new ArrayList<>(tiers);
    sorted.sort(Comparator.<Tier>comparingInt(HarvestTiersCommand::getIncorrectBlocksCount).reversed()
                          .thenComparing(tier -> getTierId(tier).toString()));
    return List.copyOf(sorted);
  }

  /** Runs the command, dumping the tag */
  private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    List<Tier> sortedTiers = getSortedTiers();

    // start building output message
    MutableComponent output = Component.translatable("command.mantle.harvest_tiers.success_list");
    // if no values, print empty
    if (sortedTiers.isEmpty()) {
      output.append("\n* ").append(EMPTY);
    } else {
      for (Tier tier : sortedTiers) {
        output.append("\n* ");
        TagKey<Block> tag = getTierTag(tier);
        ResourceLocation id = getTierId(tier);
        output.append(Component.translatable("command.mantle.harvest_tiers.tag", id, getTagComponent(tag)));
      }
    }
    context.getSource().sendSuccess(() -> output, true);
    return sortedTiers.size();
  }

  /** Runs the command, dumping the tag */
  private static int run(CommandContext<CommandSourceStack> context, boolean saveFile) throws CommandSyntaxException {
    List<Tier> sortedTiers = getSortedTiers();

    // save the list as JSON
    JsonArray entries = new JsonArray();
    for (Tier tier : sortedTiers) {
      entries.add(getTierId(tier).toString());
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
      context.getSource().sendSuccess(() -> Component.translatable("command.mantle.harvest_tiers.success_save", GeneratePackHelper.getOutputComponent(output)), true);
    } else {
      // print to console
      context.getSource().sendSuccess(() -> SUCCESS_LOG, true);
      Mantle.logger.info("Dump of harvests tiers:\n{}", DumpTagCommand.GSON.toJson(json));
    }
    // return a number to finish
    return sortedTiers.size();
  }
}

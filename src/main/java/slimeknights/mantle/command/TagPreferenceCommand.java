package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.command.argument.TagSourceArgument;
import slimeknights.mantle.recipe.helper.TagPreference;

/** Command to test tag preference behavior */
public class TagPreferenceCommand {
  private static final String EMPTY_TAG = "command.mantle.tag_preference.empty_tag";
  private static final String PREFERENCE = "command.mantle.tag_preference.preference";

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_EDIT_SPAWN))
      .then(RegistryArgument.argument().then(TagSourceArgument.tagArgument("name").executes(TagPreferenceCommand::run)));
  }

  /**
   * Runs the command
   *
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return runGeneric(context, RegistryArgument.get(context));
  }

  /**
   * Runs the command, fixing issues with generics
   *
   * @param context  Tag context
   * @return  Integer return
   */
  private static <T> int runGeneric(CommandContext<CommandSourceStack> context, Registry<T> registry) {
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);
    TagKey<T> tag = TagKey.create(registry.key(), name);
    T preference = TagPreference.getPreference(tag).orElse(null);
    if (preference == null) {
      context.getSource().sendSuccess(() -> Component.translatable(EMPTY_TAG, registry.key().location(), name), true);
      return 0;
    } else {
      context.getSource().sendSuccess(() -> Component.translatable(PREFERENCE, registry.key().location(), name, registry.getKey(preference)), true);
      return 1;
    }
  }
}

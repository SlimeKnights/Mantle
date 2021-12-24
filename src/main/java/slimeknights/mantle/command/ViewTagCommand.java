package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;

import java.util.Collection;
import java.util.Objects;

/** Command that lists all values in a tag */
public class ViewTagCommand {
  /** Tag has no values */
  private static final Component EMPTY = new TranslatableComponent("command.mantle.tag.empty");
  /** Tag type cannot be found */
  protected static final Dynamic2CommandExceptionType TAG_NOT_FOUND = new Dynamic2CommandExceptionType((type, name) -> new TranslatableComponent("command.mantle.tag.not_found", type, name));

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> MantleCommand.requiresDebugInfoOrOp(source, MantleCommand.PERMISSION_GAME_COMMANDS))
              .then(Commands.argument("type", TagCollectionArgument.collection())
                            .then(Commands.argument("name", ResourceLocationArgument.id()).suggests(MantleCommand.VALID_TAGS)
                                          .executes(ViewTagCommand::run)));
  }

  /**
   * Runs the view-tag command with the generic registry type, done to make generics happy
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static <T> int runGeneric(CommandContext<CommandSourceStack> context, TagCollectionArgument.Result<T> result) throws CommandSyntaxException {
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);
    Tag<T> tag = result.getCollection().getTag(name);
    if (tag != null) {
      // start building output message
      MutableComponent output = new TranslatableComponent("command.mantle.view_tag.success", result.getName(), name);
      Collection<T> values = tag.getValues();

      // if no values, print empty
      if (values.isEmpty()) {
        output.append("\n* ").append(EMPTY);
      } else {
        values.stream()
              .map(result::getKey)
              .sorted((a, b) -> Objects.requireNonNull(a).compareNamespaced(Objects.requireNonNull(b)))
              .forEach(value -> output.append("\n* " + Objects.requireNonNull(value)));
      }
      context.getSource().sendSuccess(output, true);
      return values.size();
    }
    throw TAG_NOT_FOUND.create(result.getName(), name);
  }

  /**
   * Runs the view-tag command
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return runGeneric(context, TagCollectionArgument.getResult(context, "type"));
  }
}

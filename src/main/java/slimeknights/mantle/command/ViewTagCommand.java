package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.registries.IForgeRegistryEntry;

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
   * Runs the view-tag command
   * @param context  Tag context
   * @return  Integer return
   * @throws CommandSyntaxException  If invalid values are passed
   */
  private static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
    TagCollectionArgument.Result result = context.getArgument("type", TagCollectionArgument.Result.class);
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);
    ITag<?> tag = result.getCollection().getTag(name);
    if (tag != null) {
      // start building output message
      IFormattableTextComponent output = new TranslationTextComponent("command.mantle.view_tag.success", result.getName(), name);
      Collection<?> values = tag.getValues();

      // if no values, print empty
      if (values.isEmpty()) {
        output.append("\n* ").append(EMPTY);
      } else {
        values.stream()
              .filter(value -> value instanceof IForgeRegistryEntry)
              .map(value -> (IForgeRegistryEntry<?>) value)
              .sorted((a, b) -> Objects.requireNonNull(a.getRegistryName()).compareNamespaced(Objects.requireNonNull(b.getRegistryName())))
              .forEach(value -> output.append("\n* " + Objects.requireNonNull(value.getRegistryName()).toString()));
      }
      context.getSource().sendSuccess(output, true);
      return values.size();
    }
    throw TAG_NOT_FOUND.create(result.getName(), name);
  }
}

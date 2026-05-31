package slimeknights.mantle.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.Collection;
import java.util.Locale;

/** Command to set a player's hunger to another value, as all creative mode methods of changing hunger are slow. */
public class HungerCommand {
  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand = subCommand.requires(sender -> sender.hasPermission(MantleCommand.PERMISSION_GAME_COMMANDS));
    for (Operation operation : Operation.values()) {
      operation.register(subCommand);
    }
  }

  /** List of each operation to run */
  @RequiredArgsConstructor
  private enum Operation {
    SET(20) {
      @Override
      public void apply(FoodData food, int hunger, float saturation) {
        food.setFoodLevel(hunger);
        food.setSaturation(Math.min(hunger, saturation));
      }
    },
    ADD(1) {
      @Override
      public void apply(FoodData food, int hunger, float saturation) {
        food.eat(hunger, saturation);
      }
    },
    SUBTRACT(0) {
      @Override
      public void apply(FoodData food, int hunger, float saturation) {
        int newFood = Math.max(0, food.getFoodLevel() - hunger);
        food.setFoodLevel(newFood);
        food.setSaturation(Mth.clamp(food.getSaturationLevel() - hunger * saturation * 2, 0, newFood));
      }
    };

    private final String name = this.name().toLowerCase(Locale.ROOT);
    /** Saturation used if its unset */
    private final float defaultSaturation;

    /** Registers this argument with the builder */
    public void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
      subCommand.then(Commands.literal(name)
        .then(Commands.argument("targets", EntityArgument.players())
          .then(Commands.argument("hunger", IntegerArgumentType.integer(0, 20))
            .executes(context -> run(context, defaultSaturation))
            .then(Commands.argument("saturation", FloatArgumentType.floatArg(0))
              .executes(context -> run(context, FloatArgumentType.getFloat(context, "saturation")))))));
    }

    /** Applies the arguments to the food stats */
    public abstract void apply(FoodData food, int hunger, float saturation);

    /** Applies this to the given context */
    public int run(CommandContext<CommandSourceStack> context, float saturation) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
      int hunger = IntegerArgumentType.getInteger(context, "hunger");

      // apply to each player
      for (Player player : players) {
        apply(player.getFoodData(), hunger, saturation);
      }

      // log success
      if (players.size() == 1) {
        Player player = players.iterator().next();
        context.getSource().sendSuccess(() -> Component.translatable("command.mantle.hunger." + name + ".single", player.getName(), hunger), true);
      } else {
        context.getSource().sendSuccess(() -> Component.translatable("command.mantle.hunger." + name + ".multiple", players.size(), hunger), true);
      }
      return players.size();
    }
  }
}

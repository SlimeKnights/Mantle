package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Command to list all tags for an entry
 */
public class TagsForCommand {
  /** Tag type cannot be found */
  protected static final Dynamic2CommandExceptionType VALUE_NOT_FOUND = new Dynamic2CommandExceptionType((type, name) -> new TranslatableComponent("command.mantle.tags_for.not_found", type, name));

  /* Missing target errors */
  private static final Component NO_HELD_BLOCK = new TranslatableComponent("command.mantle.tags_for.no_held_block");
  private static final Component NO_HELD_ENTITY = new TranslatableComponent("command.mantle.tags_for.no_held_entity");
  private static final Component NO_HELD_POTION = new TranslatableComponent("command.mantle.tags_for.no_held_potion");
  private static final Component NO_HELD_FLUID = new TranslatableComponent("command.mantle.tags_for.no_held_fluid");
  private static final Component NO_HELD_ENCHANTMENT = new TranslatableComponent("command.mantle.tags_for.no_held_enchantment");
  private static final Component NO_TARGETED_ENTITY = new TranslatableComponent("command.mantle.tags_for.no_targeted_entity");
  private static final Component NO_TARGETED_BLOCK_ENTITY = new TranslatableComponent("command.mantle.tags_for.no_targeted_block_entity");
  /** Value has no tags */
  private static final Component NO_TAGS = new TranslatableComponent("command.mantle.tags_for.no_tags");

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand.requires(source -> MantleCommand.requiresDebugInfoOrOp(source, MantleCommand.PERMISSION_GAME_COMMANDS))
              // by registry ID
              .then(Commands.literal("id")
                            .then(Commands.argument("type", RegistryArgument.registry()).suggests(MantleCommand.REGISTRY)
                                          .then(Commands.argument("name", ResourceLocationArgument.id()).suggests(MantleCommand.REGISTRY_VALUES)
                                                        .executes(TagsForCommand::runForId))))
              // held item
              .then(Commands.literal("held")
                            .then(Commands.literal("item").executes(TagsForCommand::heldItem))
                            .then(Commands.literal("block").executes(TagsForCommand::heldBlock))
                            .then(Commands.literal("enchantment").executes(TagsForCommand::heldEnchantments))
                            .then(Commands.literal("fluid").executes(TagsForCommand::heldFluid))
                            .then(Commands.literal("entity").executes(TagsForCommand::heldEntity))
                            .then(Commands.literal("potion").executes(TagsForCommand::heldPotion)))
              // targeted
              .then(Commands.literal("targeted")
                            .then(Commands.literal("block_entity").executes(TagsForCommand::targetedTileEntity))
                            .then(Commands.literal("entity").executes(TagsForCommand::targetedEntity)));
  }

  /**
   * Prints the final list of owning tags
   * @param context     Command context
   * @param registry    Registry to output
   * @param value       Value to print
   * @param <T>         Collection type
   * @return  Number of tags printed
   */
  private static <T> int printOwningTags(CommandContext<CommandSourceStack> context, Registry<T> registry, T value) {
    MutableComponent output = new TranslatableComponent("command.mantle.tags_for.success", registry.key().location(), registry.getKey(value));
    List<ResourceLocation> tags = registry.getHolder(registry.getId(value)).stream().flatMap(Holder::getTagKeys).map(TagKey::location).toList();
    if (tags.isEmpty()) {
      output.append("\n* ").append(NO_TAGS);
    } else {
      tags.stream()
          .sorted(ResourceLocation::compareNamespaced)
          .forEach(tag -> output.append("\n* " + tag));
    }
    context.getSource().sendSuccess(output, true);
    return tags.size();
  }


  /* Standard way: by ID */

  /** Runs the registry ID subcommand making generics happy */
  private static <T> int runForResult(CommandContext<CommandSourceStack> context, Registry<T> registry) throws CommandSyntaxException {
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);
    // first, fetch value
    T value = registry.get(name);
    if (value == null) {
      throw VALUE_NOT_FOUND.create(registry.key().location(), name);
    }
    return printOwningTags(context, registry, value);
  }

  /** Run the registry ID subcommand */
  private static int runForId(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Registry<?> result = RegistryArgument.getResult(context, "type");
    return runForResult(context, result);
  }


  /* Held item, can extract some data from the stack */

  /** Item tags for held item */
  private static int heldItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Item item = context.getSource().getPlayerOrException().getMainHandItem().getItem();
    return printOwningTags(context, Registry.ITEM, item);
  }

  /** Block tags for held item */
  private static int heldBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    Item item = source.getPlayerOrException().getMainHandItem().getItem();
    Block block = Block.byItem(item);
    if (block != Blocks.AIR) {
      return printOwningTags(context, Registry.BLOCK, block);
    }
    source.sendSuccess(NO_HELD_BLOCK, true);
    return 0;
  }

  /** Fluid tags for held item */
  private static int heldFluid(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    ItemStack stack = source.getPlayerOrException().getMainHandItem();
    LazyOptional<IFluidHandlerItem> capability = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
    if (capability.isPresent()) {
      IFluidHandler handler = capability.map(h -> (IFluidHandler) h).orElse(EmptyFluidHandler.INSTANCE);
      if (handler.getTanks() > 0) {
        FluidStack fluidStack = handler.getFluidInTank(0);
        if (!fluidStack.isEmpty()) {
          Fluid fluid = fluidStack.getFluid();
          return printOwningTags(context, Registry.FLUID, fluid);
        }
      }
    }
    source.sendSuccess(NO_HELD_FLUID, true);
    return 0;
  }

  /** Potion tags for held item */
  private static int heldPotion(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    ItemStack stack = source.getPlayerOrException().getMainHandItem();
    Potion potion = PotionUtils.getPotion(stack);
    if (potion != Potions.EMPTY) {
      return printOwningTags(context, Registry.POTION, potion);
    }
    source.sendSuccess(NO_HELD_POTION, true);
    return 0;
  }

  /** Block tags for held item */
  private static int heldEnchantments(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    ItemStack stack = source.getPlayerOrException().getMainHandItem();
    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
    if (!enchantments.isEmpty()) {
      int totalTags = 0;
      // print tags for each contained enchantment
      for (Enchantment enchantment : enchantments.keySet()) {
        totalTags += printOwningTags(context, Registry.ENCHANTMENT, enchantment);
      }
      return totalTags;
    }
    source.sendSuccess(NO_HELD_ENCHANTMENT, true);
    return 0;
  }

  /** Entity tags for held item */
  private static int heldEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    ItemStack stack = source.getPlayerOrException().getMainHandItem();
    if (stack.getItem() instanceof SpawnEggItem egg) {
      EntityType<?> type = egg.getType(stack.getTag());
      return printOwningTags(context, Registry.ENTITY_TYPE, type);
    }
    source.sendSuccess(NO_HELD_ENTITY, true);
    return 0;
  }


  /* Targeted, based on look vector. Leaves out anything on the debug screen */

  /**
   * Gets the tags for the fluid being looked at
   * @param context  Context
   * @return  Tags for the looked at block or entity
   * @throws CommandSyntaxException  For command errors
   */
  private static int targetedTileEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    Player player = source.getPlayerOrException();
    Level level = source.getLevel();
    BlockHitResult blockTrace = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
    if (blockTrace.getType() == HitResult.Type.BLOCK) {
      BlockEntity be = level.getBlockEntity(blockTrace.getBlockPos());
      if (be != null) {
        BlockEntityType<?> type = be.getType();
        return printOwningTags(context, Registry.BLOCK_ENTITY_TYPE, type);
      }
    }
    // failed
    source.sendSuccess(NO_TARGETED_BLOCK_ENTITY, true);
    return 0;
  }

  /**
   * Gets the tags for the entity being looked at
   * @param context  Context
   * @return  Tags for the looked at block or entity
   * @throws CommandSyntaxException  For command errors
   */
  private static int targetedEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CommandSourceStack source = context.getSource();
    Player player = source.getPlayerOrException();
    Vec3 start = player.getEyePosition(1F);
    Vec3 look = player.getLookAngle();
    double range = Objects.requireNonNull(player.getAttribute(ForgeMod.REACH_DISTANCE.get())).getValue();
    Vec3 direction = start.add(look.x * range, look.y * range, look.z * range);
    AABB bb = player.getBoundingBox().expandTowards(look.x * range, look.y * range, look.z * range).expandTowards(1, 1, 1);
    EntityHitResult entityTrace = ProjectileUtil.getEntityHitResult(source.getLevel(), player, start, direction, bb, e -> true);
    if (entityTrace != null) {
      EntityType<?> target = entityTrace.getEntity().getType();
      return printOwningTags(context, Registry.ENTITY_TYPE, target);
    }
    // failed
    source.sendSuccess(NO_TARGETED_ENTITY, true);
    return 0;
  }
}

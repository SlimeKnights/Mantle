package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import java.util.Collection;
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
  private static final Component NO_TARGETED_TILE_ENTITY = new TranslatableComponent("command.mantle.tags_for.no_targeted_tile_entity");
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
                            .then(Commands.argument("type", TagCollectionArgument.collection())
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
                            .then(Commands.literal("tile_entity").executes(TagsForCommand::targetedTileEntity))
                            .then(Commands.literal("entity").executes(TagsForCommand::targetedEntity)));
  }

  /**
   * Prints the final list of owning tags
   * @param context     Command context
   * @param collection  Tag collection
   * @param typeName    Tag name
   * @param name        Value name
   * @param value       Value to print
   * @param <T>         Collection type
   * @return  Number of tags printed
   */
  private static <T> int printOwningTags(CommandContext<CommandSource> context, ITagCollection<T> collection, ResourceLocation typeName, ResourceLocation name, Object value) {
    // TODO: not sure if there is a better way to do this, it did come out of that registry
    IFormattableTextComponent output = new TranslationTextComponent("command.mantle.tags_for.success", typeName, name);
    Collection<ResourceLocation> tags = collection.getMatchingTags((T)value);
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

  /** Run the registry ID subcommand */
  private static int runForId(CommandContext<CommandSource> context) throws CommandSyntaxException {
    TagCollectionArgument.Result result = context.getArgument("type", TagCollectionArgument.Result.class);
    ResourceLocation name = context.getArgument("name", ResourceLocation.class);

    // first, fetch value
    if (!result.getRegistry().containsKey(name)) {
      throw VALUE_NOT_FOUND.create(result.getName(), name);
    }
    Object value = result.getRegistry().getValue(name);
    if (value == null) {
      throw VALUE_NOT_FOUND.create(result.getName(), name);
    }
    return printOwningTags(context, result.getCollection(), result.getName(), name, value);
  }


  /* Held item, can extract some data from the stack */

  /** Item tags for held item */
  private static int heldItem(CommandContext<CommandSource> context) throws CommandSyntaxException {
    Item item = context.getSource().getPlayerOrException().getMainHandItem().getItem();
    return printOwningTags(context, TagCollectionManager.getInstance().getItems(), Registry.ITEM_REGISTRY.location(), Objects.requireNonNull(item.getRegistryName()), item);
  }

  /** Block tags for held item */
  private static int heldBlock(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    Item item = source.getPlayerOrException().getMainHandItem().getItem();
    Block block = Block.byItem(item);
    if (block != Blocks.AIR) {
      return printOwningTags(context, TagCollectionManager.getInstance().getBlocks(), Registry.BLOCK_REGISTRY.location(), Objects.requireNonNull(block.getRegistryName()), block);
    }
    source.sendSuccess(NO_HELD_BLOCK, true);
    return 0;
  }

  /** Fluid tags for held item */
  private static int heldFluid(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    ItemStack stack = source.getPlayerOrException().getMainHandItem();
    LazyOptional<IFluidHandlerItem> capability = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
    if (capability.isPresent()) {
      IFluidHandler handler = capability.map(h -> (IFluidHandler) h).orElse(EmptyFluidHandler.INSTANCE);
      if (handler.getTanks() > 0) {
        FluidStack fluidStack = handler.getFluidInTank(0);
        if (!fluidStack.isEmpty()) {
          Fluid fluid = fluidStack.getFluid();
          return printOwningTags(context, TagCollectionManager.getInstance().getFluids(), Registry.FLUID_REGISTRY.location(), Objects.requireNonNull(fluid.getRegistryName()), fluid);
        }
      }
    }
    source.sendSuccess(NO_HELD_FLUID, true);
    return 0;
  }

  /** Potion tags for held item */
  private static int heldPotion(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    ItemStack stack = source.getPlayerOrException().getMainHandItem();
    Potion potion = PotionUtils.getPotion(stack);
    if (potion != Potions.EMPTY) {
      ResourceLocation registry = Registry.POTION_REGISTRY.location();
      return printOwningTags(context, ForgeTagHandler.getCustomTagTypes().get(registry), registry, Objects.requireNonNull(potion.getRegistryName()), potion);
    }
    source.sendSuccess(NO_HELD_POTION, true);
    return 0;
  }

  /** Block tags for held item */
  private static int heldEnchantments(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    ItemStack stack = source.getPlayerOrException().getMainHandItem();
    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
    if (!enchantments.isEmpty()) {
      int totalTags = 0;
      ResourceLocation registry = Registry.ENCHANTMENT_REGISTRY.location();
      ITagCollection<?> enchantmentTags = ForgeTagHandler.getCustomTagTypes().get(registry);
      // print tags for each contained enchantment
      for (Enchantment enchantment : enchantments.keySet()) {
        totalTags += printOwningTags(context, enchantmentTags, registry, Objects.requireNonNull(enchantment.getRegistryName()), enchantment);
      }
      return totalTags;
    }
    source.sendSuccess(NO_HELD_ENCHANTMENT, true);
    return 0;
  }

  /** Entity tags for held item */
  private static int heldEntity(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    ItemStack stack = source.getPlayerOrException().getMainHandItem();
    if (stack.getItem() instanceof SpawnEggItem) {
      EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
      return printOwningTags(context, TagCollectionManager.getInstance().getEntityTypes(), Registry.ENTITY_TYPE_REGISTRY.location(), Objects.requireNonNull(type.getRegistryName()), type);
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
  private static int targetedTileEntity(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    PlayerEntity player = source.getPlayerOrException();
    World world = source.getLevel();
    BlockRayTraceResult blockTrace = Item.getPlayerPOVHitResult(world, player, FluidMode.NONE);
    if (blockTrace.getType() == Type.BLOCK) {
      TileEntity te = world.getBlockEntity(blockTrace.getBlockPos());
      if (te != null) {
        TileEntityType<?> type = te.getType();
        ResourceLocation registry = Registry.BLOCK_ENTITY_TYPE_REGISTRY.location();
        return printOwningTags(context, ForgeTagHandler.getCustomTagTypes().get(registry), registry, Objects.requireNonNull(type.getRegistryName()), type);
      }
    }
    // failed
    source.sendSuccess(NO_TARGETED_TILE_ENTITY, true);
    return 0;
  }

  /**
   * Gets the tags for the entity being looked at
   * @param context  Context
   * @return  Tags for the looked at block or entity
   * @throws CommandSyntaxException  For command errors
   */
  private static int targetedEntity(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    PlayerEntity player = source.getPlayerOrException();
    Vector3d start = player.getEyePosition(1F);
    Vector3d look = player.getLookAngle();
    double range = Objects.requireNonNull(player.getAttribute(ForgeMod.REACH_DISTANCE.get())).getValue();
    Vector3d direction = start.add(look.x * range, look.y * range, look.z * range);
    AxisAlignedBB bb = player.getBoundingBox().expandTowards(look.x * range, look.y * range, look.z * range).expandTowards(1, 1, 1);
    EntityRayTraceResult entityTrace = ProjectileHelper.getEntityHitResult(source.getLevel(), player, start, direction, bb, e -> true);
    if (entityTrace != null) {
      EntityType<?> target = entityTrace.getEntity().getType();
      return printOwningTags(context, TagCollectionManager.getInstance().getEntityTypes(), Registry.ENTITY_TYPE_REGISTRY.location(), Objects.requireNonNull(target.getRegistryName()), target);
    }
    // failed
    source.sendSuccess(NO_TARGETED_ENTITY, true);
    return 0;
  }
}

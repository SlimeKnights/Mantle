package slimeknights.mantle.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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
  protected static final Dynamic2CommandExceptionType VALUE_NOT_FOUND = new Dynamic2CommandExceptionType((type, name) -> new TranslationTextComponent("command.mantle.tags_for.not_found", type, name));

  /* Missing target errors */
  private static final ITextComponent NO_HELD_BLOCK = new TranslationTextComponent("command.mantle.view_tags.no_held_block");
  private static final ITextComponent NO_HELD_ENTITY = new TranslationTextComponent("command.mantle.view_tags.no_held_entity");
  private static final ITextComponent NO_HELD_POTION = new TranslationTextComponent("command.mantle.view_tags.no_held_potion");
  private static final ITextComponent NO_HELD_FLUID = new TranslationTextComponent("command.mantle.view_tags.no_held_fluid");
  private static final ITextComponent NO_HELD_ENCHANTMENT = new TranslationTextComponent("command.mantle.view_tags.no_held_enchantment");
  private static final ITextComponent NO_TARGETED_ENTITY = new TranslationTextComponent("command.mantle.view_tags.no_targeted_entity");
  private static final ITextComponent NO_TARGETED_TILE_ENTITY = new TranslationTextComponent("command.mantle.view_tags.no_targeted_tile_entity");
  /** Value has no tags */
  private static final ITextComponent NO_TAGS = new TranslationTextComponent("command.mantle.view_tags.no_tags");

  /**
   * Registers this sub command with the root command
   * @param subCommand  Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSource> subCommand) {
    subCommand.requires(source -> MantleCommand.requiresDebugInfoOrOp(source, MantleCommand.PERMISSION_GAME_COMMANDS))
              // by registry ID
              .then(Commands.literal("id")
                            .then(Commands.argument("type", TagCollectionArgument.collection())
                                          .then(Commands.argument("name", ResourceLocationArgument.resourceLocation())
                                                        .executes(TagsForCommand::runForId))))
              // held item
              .then(Commands.literal("held")
                            .then(Commands.literal("item").executes(TagsForCommand::heldItem))
                            .then(Commands.literal("block").executes(TagsForCommand::heldBlock))
                            .then(Commands.literal("enchantments").executes(TagsForCommand::heldEnchantments))
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
    Collection<ResourceLocation> tags = collection.getOwningTags((T)value);
    if (tags.isEmpty()) {
      output.appendString("\n* ").append(NO_TAGS);
    } else {
      tags.stream()
          .sorted(ResourceLocation::compareNamespaced)
          .forEach(tag -> output.appendString("\n* " + tag));
    }
    context.getSource().sendFeedback(output, true);
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
    Item item = context.getSource().asPlayer().getHeldItemMainhand().getItem();
    return printOwningTags(context, TagCollectionManager.getManager().getItemTags(), Registry.ITEM_KEY.getLocation(), Objects.requireNonNull(item.getRegistryName()), item);
  }

  /** Block tags for held item */
  private static int heldBlock(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    Item item = source.asPlayer().getHeldItemMainhand().getItem();
    Block block = Block.getBlockFromItem(item);
    if (block != Blocks.AIR) {
      return printOwningTags(context, TagCollectionManager.getManager().getBlockTags(), Registry.BLOCK_KEY.getLocation(), Objects.requireNonNull(block.getRegistryName()), block);
    }
    source.sendFeedback(NO_HELD_BLOCK, true);
    return 0;
  }

  /** Fluid tags for held item */
  private static int heldFluid(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    ItemStack stack = source.asPlayer().getHeldItemMainhand();
    LazyOptional<IFluidHandlerItem> capability = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
    if (capability.isPresent()) {
      IFluidHandler handler = capability.map(h -> (IFluidHandler) h).orElse(EmptyFluidHandler.INSTANCE);
      if (handler.getTanks() > 0) {
        FluidStack fluidStack = handler.getFluidInTank(0);
        if (!fluidStack.isEmpty()) {
          Fluid fluid = fluidStack.getFluid();
          return printOwningTags(context, TagCollectionManager.getManager().getFluidTags(), Registry.FLUID_KEY.getLocation(), Objects.requireNonNull(fluid.getRegistryName()), fluid);
        }
      }
    }
    source.sendFeedback(NO_HELD_FLUID, true);
    return 0;
  }

  /** Potion tags for held item */
  private static int heldPotion(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    ItemStack stack = source.asPlayer().getHeldItemMainhand();
    Potion potion = PotionUtils.getPotionFromItem(stack);
    if (potion != Potions.EMPTY) {
      ResourceLocation registry = Registry.POTION_KEY.getLocation();
      return printOwningTags(context, ForgeTagHandler.getCustomTagTypes().get(registry), registry, Objects.requireNonNull(potion.getRegistryName()), potion);
    }
    source.sendFeedback(NO_HELD_POTION, true);
    return 0;
  }

  /** Block tags for held item */
  private static int heldEnchantments(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    ItemStack stack = source.asPlayer().getHeldItemMainhand();
    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
    if (!enchantments.isEmpty()) {
      int totalTags = 0;
      ResourceLocation registry = Registry.ENCHANTMENT_KEY.getLocation();
      ITagCollection<?> enchantmentTags = ForgeTagHandler.getCustomTagTypes().get(registry);
      // print tags for each contained enchantment
      for (Enchantment enchantment : enchantments.keySet()) {
        totalTags += printOwningTags(context, enchantmentTags, registry, Objects.requireNonNull(enchantment.getRegistryName()), enchantment);
      }
      return totalTags;
    }
    source.sendFeedback(NO_HELD_ENCHANTMENT, true);
    return 0;
  }

  /** Entity tags for held item */
  private static int heldEntity(CommandContext<CommandSource> context) throws CommandSyntaxException {
    CommandSource source = context.getSource();
    ItemStack stack = source.asPlayer().getHeldItemMainhand();
    if (stack.getItem() instanceof SpawnEggItem) {
      EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
      return printOwningTags(context, TagCollectionManager.getManager().getEntityTypeTags(), Registry.ENTITY_TYPE_KEY.getLocation(), Objects.requireNonNull(type.getRegistryName()), type);
    }
    source.sendFeedback(NO_HELD_ENTITY, true);
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
    PlayerEntity player = source.asPlayer();
    World world = source.getWorld();
    BlockRayTraceResult blockTrace = Item.rayTrace(world, player, FluidMode.NONE);
    if (blockTrace.getType() == Type.BLOCK) {
      TileEntity te = world.getTileEntity(blockTrace.getPos());
      if (te != null) {
        TileEntityType<?> type = te.getType();
        ResourceLocation registry = Registry.BLOCK_ENTITY_TYPE_KEY.getLocation();
        return printOwningTags(context, ForgeTagHandler.getCustomTagTypes().get(registry), registry, Objects.requireNonNull(type.getRegistryName()), type);
      }
    }
    // failed
    source.sendFeedback(NO_TARGETED_TILE_ENTITY, true);
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
    PlayerEntity player = source.asPlayer();
    Vector3d start = player.getEyePosition(1F);
    Vector3d look = player.getLookVec();
    double range = Objects.requireNonNull(player.getAttribute(ForgeMod.REACH_DISTANCE.get())).getValue();
    Vector3d direction = start.add(look.x * range, look.y * range, look.z * range);
    AxisAlignedBB bb = player.getBoundingBox().expand(look.x * range, look.y * range, look.z * range).expand(1, 1, 1);
    EntityRayTraceResult entityTrace = ProjectileHelper.rayTraceEntities(source.getWorld(), player, start, direction, bb, e -> true);
    if (entityTrace != null) {
      EntityType<?> target = entityTrace.getEntity().getType();
      return printOwningTags(context, TagCollectionManager.getManager().getEntityTypeTags(), Registry.ENTITY_TYPE_KEY.getLocation(), Objects.requireNonNull(target.getRegistryName()), target);
    }
    // failed
    source.sendFeedback(NO_TARGETED_ENTITY, true);
    return 0;
  }
}

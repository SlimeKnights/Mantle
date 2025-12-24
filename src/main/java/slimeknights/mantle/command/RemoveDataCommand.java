package slimeknights.mantle.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.world.BiomeModifier;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import java.nio.file.Path;

import static net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS;

/**
 * Helpers to remove various non-recipe data.
 * @see RemoveRecipesCommand
 */
public class RemoveDataCommand {
  /* Name is invalid */
  private static final Dynamic2CommandExceptionType INVALID_REGISTRY = new Dynamic2CommandExceptionType((name, registry) -> Mantle.makeComponent("command", "key.wrong_registry", name, registry));
  // success
  /** Translation key for successfully removing structure sets */
  private static final String STRUCTURE_SET_SUCCESS = Mantle.makeDescriptionId("command", "remove_data.structure.success");
  /** Translation key for successfully removing biome modifiers */
  private static final String BIOME_MODIFIER_SUCCESS = Mantle.makeDescriptionId("command", "remove_data.biome_modifier.success");

  /**
   * Registers this sub command with the root command
   * @param subCommand Command builder
   */
  public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
    subCommand
      .then(Commands.literal("structure_set")
        .then(Commands.argument("id", ResourceKeyArgument.key(Registries.STRUCTURE_SET))
          .executes(RemoveDataCommand::removeStructureSet)))
      .then(Commands.literal("biome_modifier")
        .then(Commands.argument("id", ResourceKeyArgument.key(BIOME_MODIFIERS))
          .executes(RemoveDataCommand::removeBiomeModifier)));
  }

  /** Fetches a resource key for the given registry */
  @SuppressWarnings({"CastCanBeRemovedNarrowingVariableType", "unchecked"})
  private static <T> ResourceKey<T> getResourceKey(CommandContext<CommandSourceStack> context, String name, ResourceKey<Registry<T>> registry) throws CommandSyntaxException {
    ResourceKey<?> key = context.getArgument(name, ResourceKey.class);
    if (key.isFor(registry)) {
      return (ResourceKey<T>) key;
    }
    throw INVALID_REGISTRY.create(key, registry.location());
  }

  /** Empties the given structure set */
  private static int removeStructureSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    long startTime = System.nanoTime();
    ResourceKey<StructureSet> id = getResourceKey(context, "id", Registries.STRUCTURE_SET);

    // start by fetching the existing structure set JSON
    ResourceLocation setLocation = JsonHelper.wrap(id.location(), Registries.STRUCTURE_SET.location().getPath() + '/' , ".json");

    // determine the path for the resulting datapack
    Path pack = GeneratePackHelper.getDatapackPath(context.getSource().getServer());
    GeneratePackHelper.saveMcmeta(pack);

    // save the final JSON
    Path path = pack.resolve(PackType.SERVER_DATA.getDirectory()).resolve(setLocation.getNamespace() + '/' + setLocation.getPath());
    if (!GeneratePackHelper.saveConditionRemove(path)) {
      throw GeneratePackHelper.FAILED_SAVE.create(id);
    }

    // send success
    float time = (System.nanoTime() - startTime) / 1000000f;
    context.getSource().sendSuccess(() -> Component.translatable(STRUCTURE_SET_SUCCESS, id.location(), time, GeneratePackHelper.getOutputComponent(pack)), true);
    return 1;
  }

  /** Empties the given structure set */
  private static int removeBiomeModifier(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    long startTime = System.nanoTime();
    ResourceKey<BiomeModifier> id = getResourceKey(context, "id", BIOME_MODIFIERS);

    // start by fetching the existing structure set JSON
    ResourceLocation modifierLocation = JsonHelper.wrap(id.location(), BIOME_MODIFIERS.location().getNamespace() + '/' + BIOME_MODIFIERS.location().getPath() + '/', ".json");
    JsonObject json = new JsonObject();
    json.addProperty("type", ForgeMod.NONE_BIOME_MODIFIER_TYPE.getId().toString());

    // determine the path for the resulting datapack
    Path pack = GeneratePackHelper.getDatapackPath(context.getSource().getServer());
    GeneratePackHelper.saveMcmeta(pack);

    Path path = pack.resolve(PackType.SERVER_DATA.getDirectory()).resolve(modifierLocation.getNamespace() + '/' + modifierLocation.getPath());
    if (!GeneratePackHelper.saveJson(json, path)) {
      throw GeneratePackHelper.FAILED_SAVE.create(id.location());
    }

    // send success
    float time = (System.nanoTime() - startTime) / 1000000f;
    context.getSource().sendSuccess(() -> Component.translatable(BIOME_MODIFIER_SUCCESS, id.location(), time, GeneratePackHelper.getOutputComponent(pack)), true);
    return 1;
  }
}

package slimeknights.mantle.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus.Internal;

import static net.minecraft.commands.synchronization.SuggestionProviders.register;
import static slimeknights.mantle.Mantle.getResource;

/**
 * Argument type that supports any vanilla registry. Due to the lack of context, not a true argument type but rather helpers.
 * @see slimeknights.mantle.command.argument.TagSourceArgument
 */
public class RegistryArgument {
  /* Name is invalid */
  private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(name -> Component.translatable("command.mantle.registry.not_found", name));

  // suggestion providers
  /** Suggestion provider for a registry */
  public static SuggestionProvider<CommandSourceStack> REGISTRY;
  /** Suggestion provider for all valid tags in a registry */
  public static SuggestionProvider<CommandSourceStack> TAG;
  /** Suggestion provider for all values in a registry */
  public static SuggestionProvider<CommandSourceStack> VALUE;

  /** Creates and registers all suggestion providers */
  @Internal
  public static void registerSuggestions() {
    REGISTRY = register(getResource("registry"), (context, builder) ->
      SharedSuggestionProvider.suggestResource(context.getSource().registryAccess().registries().map(entry -> entry.key().location()), builder));
    TAG = register(getResource("registry_tags"), (context, builder) -> {
      Registry<?> result = get(context);
      return SharedSuggestionProvider.suggestResource(result.getTagNames().map(TagKey::location), builder);
    });
    VALUE = register(getResource("registry_values"), (context, builder) -> {
      Registry<?> result = get(context);
      return SharedSuggestionProvider.suggestResource(result.keySet(), builder);
    });
  }

  /** Creates an argument instance */
  public static ArgumentType<ResourceLocation> registry() {
    return ResourceLocationArgument.id();
  }

  /** Creates an argument builder with the given name */
  public static RequiredArgumentBuilder<CommandSourceStack,ResourceLocation> argument() {
    return Commands.argument("type", registry()).suggests(REGISTRY);
  }

  /** Gets the result of this argument. */
  public static Registry<?> get(CommandContext<? extends SharedSuggestionProvider> context, String name) throws CommandSyntaxException {
    ResourceLocation id = context.getArgument(name, ResourceLocation.class);
    return context.getSource().registryAccess()
                   .registry(ResourceKey.createRegistryKey(id))
                   .orElseThrow(() -> NOT_FOUND.create(id));
  }

  /** Gets the result of this argument */
  public static Registry<?> get(CommandContext<? extends SharedSuggestionProvider> context) throws CommandSyntaxException {
    return get(context, "type");
  }
}

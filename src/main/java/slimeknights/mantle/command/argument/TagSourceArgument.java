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
import net.minecraft.core.RegistryAccess.RegistryEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus.Internal;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static net.minecraft.commands.synchronization.SuggestionProviders.register;
import static slimeknights.mantle.Mantle.getResource;

/**
 * Argument type that supports any vanilla registry plus custom tag sources. Due to the lack of context, not a true argument type but rather helpers.
 * @see slimeknights.mantle.command.RegistryArgument
 */
public class TagSourceArgument {
  /* Name is invalid */
  private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(name -> Component.translatable("command.mantle.tag_source.not_found", name));
  /** Map of non-registry tag sources */
  private static final Map<ResourceKey<? extends Registry<?>>,TagSource<?>> CUSTOM_TAG_SOURCES = new HashMap<>();

  // suggestion providers
  /** Suggestion provider for a tag source */
  public static SuggestionProvider<CommandSourceStack> SOURCE;
  /** Suggestion provider for all valid tags in a tag source */
  public static SuggestionProvider<CommandSourceStack> TAG;
  /** Suggestion provider for all values in a registry */
  public static SuggestionProvider<CommandSourceStack> VALUE;
  /** Suggestion provider for all tags or values in a registry */
  public static SuggestionProvider<CommandSourceStack> ENTRY;

  /** Creates and registers all suggestion providers */
  @Internal
  public static void registerSuggestions() {
    SOURCE = register(getResource("tag_source"), (context, builder) ->
      SharedSuggestionProvider.suggestResource(allKeys(context).map(ResourceKey::location), builder));
    TAG = register(getResource("tag_source_tag"), (context, builder) -> {
      TagSource<?> result = get(context);
      return SharedSuggestionProvider.suggestResource(result.tagKeys().map(TagKey::location), builder);
    });
    VALUE = register(getResource("tag_source_value"), (context, builder) -> {
      TagSource<?> result = get(context);
      return SharedSuggestionProvider.suggestResource(result.valueKeys(), builder);
    });
    ENTRY = register(getResource("tag_source_entry"), (context, builder) -> {
      TagSource<?> result = get(context);
      // entries
      SharedSuggestionProvider.suggestResource(result.valueKeys(), builder);
      // tags
      SharedSuggestionProvider.suggestResource(result.tagKeys().map(TagKey::location), builder, "#");
      return builder.buildFuture();
    });
  }

  /** Registers a non-registry tag source */
  @SuppressWarnings("unused")
  public static <T> TagSource<T> registerCustom(TagSource<T> source) {
    TagSource<?> existing = CUSTOM_TAG_SOURCES.putIfAbsent(source.key(), source);
    if (existing != null) {
      throw new IllegalArgumentException("Duplicate custom tag source: " + source.key());
    }
    return source;
  }


  /* Argument creation */

  /** Creates an argument instance */
  public static ArgumentType<ResourceLocation> source() {
    return ResourceLocationArgument.id();
  }

  /** Creates an argument builder with the given name */
  public static RequiredArgumentBuilder<CommandSourceStack,ResourceLocation> argument() {
    return Commands.argument("type", source()).suggests(SOURCE);
  }

  /** Creates a tag argument builder with the given name */
  public static RequiredArgumentBuilder<CommandSourceStack,ResourceLocation> tagArgument(String key) {
    return Commands.argument(key, ResourceLocationArgument.id()).suggests(TAG);
  }

  /** Creates a value argument builder with the given name */
  public static RequiredArgumentBuilder<CommandSourceStack,ResourceLocation> valueArgument(String key) {
    return Commands.argument(key, ResourceLocationArgument.id()).suggests(VALUE);
  }

  /** Creates an entry (tag or value) argument builder with the given name */
  public static RequiredArgumentBuilder<CommandSourceStack,ResourceOrTagKeyArgument.Result> entryArgument(String key) {
    return Commands.argument(key, ResourceOrTagKeyArgument.key()).suggests(ENTRY);
  }

  /** Gets a stream of all tag source keys */
  public static Stream<TagSource<?>> allSources(CommandContext<? extends SharedSuggestionProvider> context) {
    return Stream.concat(
      context.getSource().registryAccess().registries().map(entry -> new RegistryTagSource<>(entry.value())),
      CUSTOM_TAG_SOURCES.values().stream()
    );
  }

  /** Gets a stream of all tag source keys */
  public static Stream<ResourceKey<? extends Registry<?>>> allKeys(CommandContext<? extends SharedSuggestionProvider> context) {
    return Stream.concat(
      context.getSource().registryAccess().registries().map(RegistryEntry::key),
      CUSTOM_TAG_SOURCES.keySet().stream()
    );
  }


  /* Argument fetching */

  /** Gets the result of this argument */
  public static TagSource<?> get(CommandContext<? extends SharedSuggestionProvider> context) throws CommandSyntaxException {
    ResourceLocation id = context.getArgument("type", ResourceLocation.class);
    ResourceKey<? extends Registry<?>> key = ResourceKey.createRegistryKey(id);
    // try a custom source first, saves a lookup to the registry
    TagSource<?> custom = CUSTOM_TAG_SOURCES.get(key);
    if (custom != null) {
      return custom;
    }
    return new RegistryTagSource<>(context.getSource().registryAccess()
      .registry(key)
      .orElseThrow(() -> NOT_FOUND.create(id)));
  }
}

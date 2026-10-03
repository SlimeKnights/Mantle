package slimeknights.mantle.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider.ElementSuggestionType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.command.argument.ResourceOrTagKeyArgument.Result;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/** Argument that can be either a tag or a value. Like {@link net.minecraft.commands.arguments.ResourceOrTagKeyArgument} but without a required registry. */
public record ResourceOrTagKeyArgument<T>(@Nullable ResourceKey<? extends Registry<T>> registry) implements ArgumentType<Result> {
  private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");

  /** Creates an argument */
  public static ResourceOrTagKeyArgument<?> key() {
    return new ResourceOrTagKeyArgument<>(null);
  }

  /** Creates an argument */
  public static <T> ResourceOrTagKeyArgument<T> registry(ResourceKey<? extends Registry<T>> registry) {
    return new ResourceOrTagKeyArgument<>(registry);
  }

  /** Gets the result */
  public static Result get(CommandContext<CommandSourceStack> context, String key) {
    return context.getArgument(key, Result.class);
  }

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    // tag
    if (reader.canRead() && reader.peek() == '#') {
      int pos = reader.getCursor();
      try {
        reader.skip();
        return new Result(ResourceLocation.read(reader), true);
      } catch (CommandSyntaxException ex) {
        reader.setCursor(pos);
        throw ex;
      }
    }
    return new Result(ResourceLocation.read(reader), false);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    if (registry != null && context.getSource() instanceof SharedSuggestionProvider suggestions) {
      return suggestions.suggestRegistryElements(registry, ElementSuggestionType.ALL, builder, context);
    }
    return Suggestions.empty();
  }

  @Override
  public Collection<String> getExamples() {
    return EXAMPLES;
  }

  /** Result for this argument type */
  public record Result(ResourceLocation location, boolean isTag) {
    /** Creates a resource key from the result */
    public <T> TagKey<T> resource(ResourceKey<? extends Registry<T>> registry) {
      if (isTag) {
        throw new IllegalStateException("Cannot create a resource key for a tag result");
      } else {
        return TagKey.create(registry, location);
      }
    }

    /** Creates a tag key from the result */
    public <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registry) {
      if (isTag) {
        return TagKey.create(registry, location);
      } else {
        throw new IllegalStateException("Cannot create a tag for a value result");
      }
    }

    /** Creates a tag entry from this result */
    public TagEntry tagEntry() {
      if (isTag) {
        return TagEntry.tag(location);
      } else {
        return TagEntry.element(location);
      }
    }

    /** Gets a string representation of this result */
    @Override
    public String toString() {
      if (isTag) {
        return '#' + location.toString();
      }
      return location.toString();
    }
  }

  /** Apparently you need all this to serialize argument info */
  public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ResourceOrTagKeyArgument.Info<T>.Template> {
    @Override
    public void serializeToJson(Template template, JsonObject json) {
      if (template.registry != null) {
        json.addProperty("registry", template.registry.location().toString());
      }
    }

    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
      if (template.registry != null) {
        buffer.writeResourceLocation(template.registry.location());
      } else {
        buffer.writeUtf("");
      }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
      String str = buffer.readUtf(Short.MAX_VALUE);
      if (str.isEmpty()) {
        return new Template(null);
      }
      return new Template(ResourceKey.createRegistryKey(ResourceLocation.parse(str)));
    }

    @Override
    public Template unpack(ResourceOrTagKeyArgument<T> argument) {
      return new Template(argument.registry);
    }

    @RequiredArgsConstructor
    public class Template implements ArgumentTypeInfo.Template<ResourceOrTagKeyArgument<T>> {
      @Nullable
      private final ResourceKey<? extends Registry<T>> registry;

      @Override
      public ResourceOrTagKeyArgument<T> instantiate(CommandBuildContext commandBuildContext) {
        return new ResourceOrTagKeyArgument<>(registry);
      }

      @Override
      public ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ?> type() {
        return Info.this;
      }
    }
  }
}

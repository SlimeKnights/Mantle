package slimeknights.mantle.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.TagCollection;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Argument type that supports any tag collection
 */
@RequiredArgsConstructor(staticName = "collection")
public class TagCollectionArgument implements ArgumentType<TagCollectionArgument.Result<?>> {
  private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:blocks", "minecraft:enchantments");
  // errors
  /* Tag collection name is invalid */
  private static final DynamicCommandExceptionType TAG_COLLECTION_NOT_FOUND = new DynamicCommandExceptionType(name -> new TranslatableComponent("command.mantle.tag_collection.not_found", name));

  /** Gets the result of this argument */
  public static Result<?> getResult(CommandContext<CommandSourceStack> pContext, String pName) {
    return pContext.getArgument(pName, Result.class);
  }

  /**
   * Gets the tag folder for the given registry key
   * @param name  Registry key
   * @return  Tag folder
   */
  public static String getTagFolder(ResourceLocation name) {
    return getTagFolder(RegistryManager.ACTIVE.getRegistry(name), name);
  }

  /**
   * Gets the tag folder given a registry and name
   * @param registry  Registry
   * @param name      Name for fallback if the registry has no tag folder
   * @return  Tag folder name
   */
  public static String getTagFolder(@Nullable ForgeRegistry<?> registry, ResourceLocation name) {
    if (registry != null) {
      String tagFolder = registry.getTagFolder();
      if (tagFolder != null) {
        return tagFolder;
      }
    }
    return name.getPath() + "s";
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Result<?> parse(StringReader reader) throws CommandSyntaxException {
    ResourceLocation name = ResourceLocation.read(reader);
    TagCollection<?> collection = SerializationTags.getInstance().get(ResourceKey.createRegistryKey(name));
    if (collection != null) {
      ForgeRegistry<?> forgeRegistry = RegistryManager.ACTIVE.getRegistry(name);
      String tagFolder = getTagFolder(forgeRegistry, name);
      if (forgeRegistry != null) {
        return new ForgeResult(name, tagFolder, collection, forgeRegistry);
      }
      Registry<?> registry = Registry.REGISTRY.get(name);
      if (registry != null) {
        return new VanillaResult(name, tagFolder, collection, registry);
      }
    }
    throw TAG_COLLECTION_NOT_FOUND.create(name);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    return SharedSuggestionProvider.suggestResource(SerializationTags.getInstance().collections.keySet().stream().map(ResourceKey::location), builder);
  }

  @Override
  public Collection<String> getExamples() {
    return EXAMPLES;
  }

  /** Data class for result, to ensure its unique to the mod */
  @RequiredArgsConstructor
  public static abstract class Result<T> {
    @Getter
    private final ResourceLocation name;
    @Getter
    private final String tagFolder;
    @Getter
    private final TagCollection<T> collection;

    /** Gets all keys associated with the given registry */
    public abstract Collection<ResourceLocation> getKeys();

    /** Gets the key for the given object */
    @Nullable
    public abstract ResourceLocation getKey(T object);

    /** Gets the value for the given key */
    @Nullable
    public abstract T getValue(ResourceLocation key);
  }

  /** Result for a forge registry */
  private static class ForgeResult<T extends IForgeRegistryEntry<T>> extends Result<T> {
    private final ForgeRegistry<T> registry;
    public ForgeResult(ResourceLocation name, String tagFolder, TagCollection<T> collection, ForgeRegistry<T> registry) {
      super(name, tagFolder, collection);
      this.registry = registry;
    }

    @Override
    public Collection<ResourceLocation> getKeys() {
      return registry.getKeys();
    }

    @Override
    public ResourceLocation getKey(T object) {
      return object.getRegistryName();
    }

    @Nullable
    @Override
    public T getValue(ResourceLocation key) {
      if (registry.containsKey(key)) {
        return registry.getValue(key);
      }
      return null;
    }
  }

  /** Result for a vanilla registry */
  private static class VanillaResult<T> extends Result<T> {
    private final Registry<T> registry;
    public VanillaResult(ResourceLocation name, String tagFolder, TagCollection<T> collection, Registry<T> registry) {
      super(name, tagFolder, collection);
      this.registry = registry;
    }

    @Override
    public Collection<ResourceLocation> getKeys() {
      return registry.keySet();
    }

    @Override
    public ResourceLocation getKey(T object) {
      return registry.getKey(object);
    }

    @Nullable
    @Override
    public T getValue(ResourceLocation key) {
      if (registry.containsKey(key)) {
        return registry.get(key);
      }
      return null;
    }
  }
}

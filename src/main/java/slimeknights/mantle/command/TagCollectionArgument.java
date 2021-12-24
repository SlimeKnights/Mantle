package slimeknights.mantle.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.NoArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
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
@NoArgsConstructor(staticName = "collection")
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
      ForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(name);
      String tagFolder = getTagFolder(registry, name);
      return new Result(name, tagFolder, registry, collection);
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
  public record Result<T extends IForgeRegistryEntry<T>>(ResourceLocation name, String tagFolder, ForgeRegistry<T> registry, TagCollection<T> collection) {}
}

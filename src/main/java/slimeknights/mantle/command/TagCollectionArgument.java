package slimeknights.mantle.command;

import com.google.common.collect.AbstractIterator;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Argument type that supports any tag collection
 */
@NoArgsConstructor(staticName = "collection")
public class TagCollectionArgument implements ArgumentType<TagCollectionArgument.Result> {
  private static final Collection<String> EXAMPLES = Arrays.asList("minecraft:blocks", "minecraft:enchantments");
  private static final Iterable<ResourceLocation> ALL_COLLECTIONS_ITERABLE = CollectionIterator::new;
  // errors
  /* Tag collection name is invalid */
  private static final DynamicCommandExceptionType TAG_COLLECTION_NOT_FOUND = new DynamicCommandExceptionType(name -> new TranslationTextComponent("command.mantle.tag_collection.not_found", name));

  /**
   * Gets the tag type for a vanilla tag
   * @param name  Name
   * @return  Tag type
   */
  @Nullable
  private static Result getVanillaTags(ForgeRegistry<?> registry, ResourceLocation name) {
    for (VanillaTagType type : VanillaTagType.values()) {
      if (name.equals(type.getName())) {
        return Result.of(name, type.getTagFolder(), registry, type.getCollection());
      }
    }
    return null;
  }

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    ResourceLocation name = ResourceLocation.read(reader);
    // first, find the proper registry, if no registry the tag is probably the wrong type
    ForgeRegistry<?> registry = RegistryManager.ACTIVE.getRegistry(name);
    if (registry != null) {
      // try vanilla tags next
      Result result = getVanillaTags(registry, name);
      if (result != null) {
        return result;
      }

      // then forge tags
      ITagCollection<?> collection = ForgeTagHandler.getCustomTagTypes().get(name);
      String tagFolder = registry.getTagFolder();
      if (collection != null && tagFolder != null) {
        return Result.of(name, tagFolder, registry, collection);
      }
    }
    throw TAG_COLLECTION_NOT_FOUND.create(name);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    return ISuggestionProvider.suggestResource(ALL_COLLECTIONS_ITERABLE, builder);
  }

  @Override
  public Collection<String> getExamples() {
    return EXAMPLES;
  }

  /** Data class for result, to ensure its unique to the mod */
  @Data(staticConstructor = "of")
  public static class Result {
    private final ResourceLocation name;
    private final String tagFolder;
    private final ForgeRegistry<?> registry;
    private final ITagCollection<?> collection;
  }

  /** Enum containing all vanilla tag collection types */
  @AllArgsConstructor
  protected enum VanillaTagType {
    BLOCK(Registry.BLOCK_REGISTRY.location(),             () -> TagCollectionManager.getInstance().getBlocks()),
    ITEM(Registry.ITEM_REGISTRY.location(),               () -> TagCollectionManager.getInstance().getItems()),
    FLUID(Registry.FLUID_REGISTRY.location(),             () -> TagCollectionManager.getInstance().getFluids()),
    ENTITY_TYPE(Registry.ENTITY_TYPE_REGISTRY.location(), () -> TagCollectionManager.getInstance().getEntityTypes());

    @Getter
    private final ResourceLocation name;
    private final Supplier<ITagCollection<?>> supplier;

    public ITagCollection<?> getCollection() {
      return supplier.get();
    }

    public String getTagFolder() {
      return name.getPath() + "s";
    }
  }

  /** Iterator for all possible collection names */
  private static class CollectionIterator extends AbstractIterator<ResourceLocation> {
    private VanillaTagType nextVanilla = VanillaTagType.BLOCK;
    private final Iterator<ResourceLocation> forgeIterator = ForgeTagHandler.getCustomTagTypeNames().iterator();

    @Override
    protected ResourceLocation computeNext() {
      // iterate vanilla tags first
      if (nextVanilla != null) {
        ResourceLocation result = nextVanilla.getName();
        // simply increase index by 1 through enum
        int nextIndex = nextVanilla.ordinal() + 1;
        VanillaTagType[] values = VanillaTagType.values();
        if (nextIndex < values.length) {
          nextVanilla = values[nextIndex];
        } else {
          // null means time for forge tags
          nextVanilla = null;
        }
        return result;
        // for forge, just redirect to the collection iterator
      } else if (forgeIterator.hasNext()) {
        return forgeIterator.next();
      }
      return endOfData();
    }
  }
}

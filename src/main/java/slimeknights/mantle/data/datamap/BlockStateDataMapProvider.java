package slimeknights.mantle.data.datamap;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.data.loadable.Loadable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

/** Data provider for {@link BlockStateDataMapLoader} */
public abstract class BlockStateDataMapProvider<D> extends GenericDataProvider {
  private final Loadable<D> dataLoader;
  private final String modId;
  private final Map<Block,DataMap> blocks = new HashMap<>();
  private final Map<ResourceLocation,D> entries = new HashMap<>();
  public BlockStateDataMapProvider(PackOutput output, Target type, String folder, Loadable<D> dataLoader, String modId) {
    super(output, type, folder);
    this.dataLoader = dataLoader;
    this.modId = modId;
  }

  public BlockStateDataMapProvider(PackOutput output, Target type, BlockStateDataMapLoader<D> registry, String modId) {
    this(output, type, registry.getFolder(), registry.getDataLoader(), modId);
  }

  /** Adds all entries to this provider */
  protected abstract void addEntries();

  @Override
  public CompletableFuture<?> run(CachedOutput cached) {
    addEntries();
    return allOf(Stream.concat(
      blocks.values().stream().map(entry -> saveJson(cached, BuiltInRegistries.BLOCK.getKey(entry.owner), entry.toJson())),
      entries.entrySet().stream().map(entry -> saveJson(cached, entry.getKey(), dataLoader.serialize(entry.getValue())))
    ));
  }

  /** Creates a new builder for a block */
  protected DataMap block(Block block) {
    return blocks.computeIfAbsent(block, DataMap::new);
  }

  /** Creates a new builder for a block */
  protected DataMap block(Supplier<? extends Block> block) {
    return block(block.get());
  }

  /** Adds an entry that a block may redirect to */
  protected void entry(ResourceLocation key, D data) {
    D original = entries.putIfAbsent(key, data);
    if (original != null) {
      throw new IllegalArgumentException("Duplicate entry at " + key + ", original " + original + ", new value " + data);
    }
  }

  /** Adds an entry that a block may redirect to */
  protected void entry(String key, D data) {
    entry(ResourceLocation.fromNamespaceAndPath(modId, key), data);
  }

  /** Record holding a single entry in the variants list */
  private record Variant<D>(@Nullable D data, @Nullable ResourceLocation parent, StateVariantStringBuilder variant) {}

  /** Represents a single file to be generated */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  protected class DataMap {
    private final Block owner;
    private final List<Variant<D>> variants = new ArrayList<>();

    /** Add a data variant, which is stored directly in the JSON */
    public VariantBuilder variant(D data) {
      VariantBuilder builder = new VariantBuilder();
      variants.add(new Variant<>(data, null, builder));
      return builder;
    }

    /** Adds a parent variant, stored as a string */
    public VariantBuilder variant(ResourceLocation parent) {
      VariantBuilder builder = new VariantBuilder();
      variants.add(new Variant<>(null, parent, builder));
      return builder;
    }

    /** Adds a parent variant, stored as a string */
    public VariantBuilder variant(String parent) {
      return variant(ResourceLocation.fromNamespaceAndPath(modId, parent));
    }

    /** Serializes this to JSON */
    private JsonObject toJson() {
      JsonObject variants = new JsonObject();
      for (Variant<D> variant : this.variants) {
        String variantString = variant.variant.toString();
        if (variant.data != null) {
          variants.add(variantString, dataLoader.serialize(variant.data));
        } else if (variant.parent != null) {
          variants.addProperty(variantString, variant.parent.toString());
        }
      }
      JsonObject map = new JsonObject();
      map.add("variants", variants);
      return map;
    }

    /** Variant builder that returns to the data map when finished */
    public class VariantBuilder extends StateVariantStringBuilder {
      protected VariantBuilder() {
        super(owner);
      }

      @Override
      public <T extends Comparable<T>> VariantBuilder when(Property<T> prop, T value) {
        super.when(prop, value);
        return this;
      }

      /** Returns to the outer datamap */
      public DataMap end() {
        return DataMap.this;
      }
    }
  }
}

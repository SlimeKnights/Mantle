package slimeknights.mantle.data.datamap;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/** Data provider for {@link RegistryDataMapLoader} */
public abstract class RegistryDataMapProvider<R,D> extends GenericDataProvider {
  private final Registry<R> registry;
  private final RecordLoadable<D> dataLoader;
  private final String modId;
  private final Map<ResourceLocation,Supplier<JsonObject>> entries = new HashMap<>();

  public RegistryDataMapProvider(PackOutput output, Target type, Registry<R> registry, RecordLoadable<D> dataLoader, String folder, String modId) {
    super(output, type, folder);
    this.registry = registry;
    this.dataLoader = dataLoader;
    this.modId = modId;
  }

  public RegistryDataMapProvider(PackOutput output, Target type, RegistryDataMapLoader<R,D> dataLoader, String modId) {
    this(output, type, dataLoader.getRegistry(), dataLoader.getDataLoader(), dataLoader.getFolder(), modId);
  }

  /** Adds all entries to this provider */
  protected abstract void addEntries();

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    addEntries();
    return allOf(entries.entrySet().stream().map(entry -> saveJson(cache, entry.getKey(), entry.getValue().get())));
  }


  /* Provider helpers */

  /** Makes a location from a path */
  protected ResourceLocation key(String name) {
    return ResourceLocation.fromNamespaceAndPath(modId, name);
  }

  /** Makes a location from a registry entry */
  protected ResourceLocation key(R entry) {
    return Objects.requireNonNull(registry.getKey(entry));
  }

  /** Makes a location from a registry entry */
  protected ResourceLocation key(Supplier<? extends R> entry) {
    return key(entry.get());
  }


  /* Basic supplier methods */

  /** Adds an entry to the provider */
  protected void entry(ResourceLocation key, Supplier<JsonObject> json) {
    Supplier<JsonObject> original = entries.putIfAbsent(key, json);
    if (original != null) {
      throw new IllegalArgumentException("Duplicate entry at " + key + ", original " + original + ", new value " + json);
    }
  }


  /* Redirects */

  /** Adds a redirect to the provider */
  protected void redirect(ResourceLocation key, ResourceLocation parent) {
    entry(key, () -> {
      JsonObject json = new JsonObject();
      json.addProperty("parent", parent.toString());
      return json;
    });
  }

  /** Adds a redirect to the provider */
  protected void redirect(String key, ResourceLocation parent) {
    redirect(key(key), parent);
  }

  /** Adds a redirect to the provider */
  protected void redirect(R key, ResourceLocation parent) {
    redirect(key(key), parent);
  }

  /** Adds a redirect to the provider */
  protected void redirect(Supplier<? extends R> key, ResourceLocation parent) {
    redirect(key(key), parent);
  }

  /** Adds a redirect to the provider */
  protected void redirect(String key, String parent) {
    redirect(key(key), key(parent));
  }

  /** Adds a redirect to the provider */
  protected void redirect(R key, String parent) {
    redirect(key(key), key(parent));
  }

  /** Adds a redirect to the provider */
  protected void redirect(Supplier<? extends R> key, String parent) {
    redirect(key(key), key(parent));
  }


  /* Data entry methods */

  /** Adds a full data object to the provider */
  protected void entry(ResourceLocation key, D data) {
    entry(key, new DataEntry<>(dataLoader, data));
  }

  /** Adds a full data object to the provider */
  protected void entry(String key, D data) {
    entry(key(key), data);
  }

  /** Adds a full data object to the provider */
  protected void entry(R key, D data) {
    entry(key(key), data);
  }

  /** Adds a full data object to the provider */
  protected void entry(Supplier<? extends R> key, D data) {
    entry(key(key), data);
  }

  /** Result that serializes a piece of data */
  private record DataEntry<D>(RecordLoadable<D> loadable, D data) implements Supplier<JsonObject> {
    @Override
    public JsonObject get() {
      JsonObject json = new JsonObject();
      loadable.serialize(data, json);
      return json;
    }

    @Override
    public String toString() {
      return data.toString();
    }
  }
}

package slimeknights.mantle.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/** Data provider for data using registry access. */
public abstract class GenericRegistryDataProvider extends GenericDataProvider {
  private final CompletableFuture<HolderLookup.Provider> registries;

  public GenericRegistryDataProvider(PackOutput output, Target type, String folder, CompletableFuture<HolderLookup.Provider> registries) {
    super(output, type, folder);
    this.registries = registries;
  }

  public GenericRegistryDataProvider(PackOutput output, String folder, CompletableFuture<HolderLookup.Provider> registries) {
    this(output, Target.DATA_PACK, folder, registries);
  }

  @Override
  public final CompletableFuture<?> run(CachedOutput output) {
    return this.registries.thenCompose(lookup -> this.run(output, lookup));
  }

  /** Runs the data generator with the registry getter resolved. */
  protected abstract CompletionStage<?> run(CachedOutput output, HolderLookup.Provider lookup);

}

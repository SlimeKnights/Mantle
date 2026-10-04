package slimeknights.mantle.data;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/** Tag provider for any registry from {@link net.minecraft.core.registries.BuiltInRegistries} that lacks a standard tag provider. */
public abstract class BuiltinRegistryTagProvider<T> extends IntrinsicHolderTagsProvider<T> {
  public BuiltinRegistryTagProvider(PackOutput packOutput, Registry<T> registry, CompletableFuture<Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper) {
    super(packOutput, registry.key(), lookupProvider,
      // not sure why fetching the resource key from the object is such a pain
      value -> registry.getHolder(registry.getId(value)).orElseThrow().key(),
      modId, existingFileHelper);
  }
}

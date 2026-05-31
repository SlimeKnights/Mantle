package slimeknights.mantle.fluid.texture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.neoforged.neoforge.fluids.FluidType;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Data provider for {@link FluidTexture}
 */
@SuppressWarnings("unused")
public abstract class AbstractFluidTextureProvider extends GenericDataProvider {
  private final Map<FluidType,FluidTexture.Builder> allTextures = new HashMap<>();
  private final Set<FluidType> ignore = new HashSet<>();
  @Nullable
  private final String modId;

  public AbstractFluidTextureProvider(PackOutput packOutput, @Nullable String modId) {
    super(packOutput, Target.RESOURCE_PACK, FluidTextureManager.FOLDER, JsonHelper.DEFAULT_GSON);
    this.modId = modId;
  }

  @Override
  public final CompletableFuture<?> run(CachedOutput cache) {
    ensureTexturesAdded();
    Registry<FluidType> fluidTypeRegistry = NeoForgeRegistries.FLUID_TYPES;

    // ensure we added textures for all our fluid types
    if (modId != null) {
      List<String> missing = fluidTypeRegistry.entrySet().stream().filter(entry -> entry.getKey().location().getNamespace().equals(modId) && !allTextures.containsKey(entry.getValue()) && !ignore.contains(entry.getValue())).map(e -> e.getKey().location().toString()).toList();
      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing fluid textures for: " + String.join(", ", missing));
      }
    }
    // save files
    return allOf(allTextures.entrySet().stream().map(entry -> saveJson(cache, Objects.requireNonNull(fluidTypeRegistry.getKey(entry.getKey())), entry.getValue().build().serialize())));
  }

  /** Adds the textures if not already added */
  private void ensureTexturesAdded() {
    if (allTextures.isEmpty()) {
      addTextures();
    }
  }

  /** Gets the map of all textures. Shouod not be called in {@link #addTextures()}, meant for other data generators to use. */
  public Map<FluidType,FluidTexture.Builder> getAllTextures() {
    ensureTexturesAdded();
    return allTextures;
  }

  /** Override to add your textures at the proper time */
  public abstract void addTextures();

  /** Create a new builder for the give fluid type */
  public FluidTexture.Builder texture(FluidType fluid) {
    return allTextures.computeIfAbsent(fluid, FluidTexture.Builder::new);
  }

  /** Create a new builder for the give fluid type */
  public FluidTexture.Builder texture(FluidObject<?> fluid) {
    return texture(fluid.getType());
  }

  /** Create a new builder for the give fluid type */
  public FluidTexture.Builder texture(DeferredHolder<FluidType, ? extends FluidType> fluid) {
    return texture(fluid.get());
  }

  /** Marks the given fluid type to be ignored by this texture provider */
  public void skip(FluidType fluid) {
    ignore.add(fluid);
  }

  /** Marks the given fluid type to be ignored by this texture provider */
  public void skip(FluidObject<?> fluid) {
    skip(fluid.getType());
  }

  /** Marks the given fluid type to be ignored by this texture provider */
  public void skip(DeferredHolder<FluidType, ? extends FluidType> fluid) {
    skip(fluid.get());
  }
}

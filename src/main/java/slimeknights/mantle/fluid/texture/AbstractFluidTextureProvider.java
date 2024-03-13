package slimeknights.mantle.fluid.texture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Data provider for {@link FluidTexture}
 */
@SuppressWarnings("unused")
public abstract class AbstractFluidTextureProvider extends GenericDataProvider {
  private final Map<FluidType,FluidTexture.Builder> allTextures = new HashMap<>();
  private final Set<FluidType> ignore = new HashSet<>();
  @Nullable
  private final String modId;

  public AbstractFluidTextureProvider(DataGenerator generator, @Nullable String modId) {
    super(generator, PackType.CLIENT_RESOURCES, FluidTextureManager.FOLDER, JsonHelper.DEFAULT_GSON);
    this.modId = modId;
  }

  @Override
  public final void run(CachedOutput cache) throws IOException {
    addTextures();
    IForgeRegistry<FluidType> fluidTypeRegistry = ForgeRegistries.FLUID_TYPES.get();

    // ensure we added textures for all our fluid types
    if (modId != null) {
      List<String> missing = fluidTypeRegistry.getEntries().stream().filter(entry -> entry.getKey().location().getNamespace().equals(modId) && !allTextures.containsKey(entry.getValue()) && !ignore.contains(entry.getValue())).map(e -> e.getKey().location().toString()).toList();
      if (!missing.isEmpty()) {
        throw new IllegalStateException("Missing fluid textures for: " + String.join(", ", missing));
      }
    }
    // save files
    allTextures.forEach((type, data) -> saveJson(cache, Objects.requireNonNull(fluidTypeRegistry.getKey(type)), data.build().serialize()));
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
  public FluidTexture.Builder texture(RegistryObject<? extends FluidType> fluid) {
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
  public void skip(RegistryObject<? extends FluidType> fluid) {
    skip(fluid.get());
  }
}

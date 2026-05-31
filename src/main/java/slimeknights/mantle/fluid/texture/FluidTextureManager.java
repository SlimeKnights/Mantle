package slimeknights.mantle.fluid.texture;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.listener.IEarlySafeManagerReloadListener;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Manager for handling fluid textures */
public class FluidTextureManager implements IEarlySafeManagerReloadListener {
  /** Folder containing the logic */
  public static final String FOLDER = "mantle/fluid_texture";

  /* Instance data */
  private static final FluidTextureManager INSTANCE = new FluidTextureManager();
  /** Map of fluid type to texture */
  private Map<FluidType,FluidTexture> textures = Collections.emptyMap();
  /** Fallback texture instance */
  private static final FluidTexture FALLBACK = new FluidTexture(ResourceLocation.withDefaultNamespace("block/water_still"), ResourceLocation.withDefaultNamespace("block/water_flow"), null, null, 0, -1, -1, false, null, 0, 0);

  private FluidTextureManager() {}


  /**
   * Initializes this manager, registering it with the resource manager
   */
  public static void init(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(INSTANCE);
  }

  @Override
  public void onReloadSafe(ResourceManager resourceManager) {
    long time = System.nanoTime();
    // fetch JSONs
    Map<ResourceLocation,JsonElement> jsons = new HashMap<>();
    SimpleJsonResourceReloadListener.scanDirectory(resourceManager, FOLDER, JsonHelper.DEFAULT_GSON, jsons);

    // start building fluid type map
    Map<FluidType, FluidTexture> map = new HashMap<>();
    Registry<FluidType> fluidTypeRegistry = NeoForgeRegistries.FLUID_TYPES;


    for (Map.Entry<ResourceLocation,JsonElement> entry : jsons.entrySet()) {
      ResourceLocation id = entry.getKey();
      // first step is to find the matching fluid type, if there is none ignore the file
      FluidType type = fluidTypeRegistry.get(id);
      if (type == null || !id.equals(fluidTypeRegistry.getKey(type))) {
        Mantle.logger.debug("Ignoring fluid texture {} as no fluid type exists with that name", id);
      } else {
        // parse it if valid
        map.put(type, FluidTexture.deserialize(GsonHelper.convertToJsonObject(entry.getValue(), "fluid_texture")));
      }
    }
    this.textures = map;
    Mantle.logger.info("Loaded {} fluid textures in {} ms", map.size(), (System.nanoTime() - time) / 1000000f);
  }

  /** Gets the texture for the given fluid */
  public static FluidTexture getData(FluidType fluid) {
    return INSTANCE.textures.getOrDefault(fluid, FALLBACK);
  }

  /** Gets the still texture for the given fluid */
  public static ResourceLocation getStillTexture(FluidType fluid) {
    return getData(fluid).still();
  }

  /** Gets the flowing texture for the given fluid */
  public static ResourceLocation getFlowingTexture(FluidType fluid) {
    return getData(fluid).flowing();
  }

  /** Gets the overlay texture for the given fluid */
  @Nullable
  public static ResourceLocation getOverlayTexture(FluidType fluid) {
    return getData(fluid).overlay();
  }

  /** Gets the camera texture for the given fluid */
  @Nullable
  public static ResourceLocation getCameraTexture(FluidType fluid) {
    return getData(fluid).camera();
  }

  /** Gets the still texture for the given fluid */
  public static int getColor(FluidType fluid) {
    return getData(fluid).color();
  }
}

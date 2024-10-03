package slimeknights.mantle.fluid.texture;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Manager for handling fluid tooltips */
public class FluidTextureManager extends SimpleJsonResourceReloadListener {
  /** Folder containing the logic */
  public static final String FOLDER = "mantle/fluid_texture";

  /* Instance data */
  private static final FluidTextureManager INSTANCE = new FluidTextureManager();
  /** Map of fluid type to texture */
  private Map<FluidType,FluidTexture> textures = Collections.emptyMap();
  /** Fallback texture instance */
  private static final FluidTexture FALLBACK = new FluidTexture(new ResourceLocation("block/water_still"), new ResourceLocation("block/water_flow"), null, null, -1);

  private FluidTextureManager() {
    super(JsonHelper.DEFAULT_GSON, FOLDER);
  }

  /**
   * Initializes this manager, registering it with the resource manager
   */
  public static void init(RegisterClientReloadListenersEvent event) {
    event.registerReloadListener(INSTANCE);
  }

  @Override
  public void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
    long time = System.nanoTime();
    Map<FluidType, FluidTexture> map = new HashMap<>();
    IForgeRegistry<FluidType> fluidTypeRegistry = ForgeRegistries.FLUID_TYPES.get();

    for (Map.Entry<ResourceLocation,JsonElement> entry : jsons.entrySet()) {
      ResourceLocation fullPath = entry.getKey();
      ResourceLocation id = JsonHelper.localize(fullPath, FOLDER, ".json");
      // first step is to find the matching fluid type, if there is none ignore the file
      FluidType type = fluidTypeRegistry.getValue(id);
      if (type == null || !id.equals(fluidTypeRegistry.getKey(type))) {
        Mantle.logger.debug("Ignoring fluid texture {} from {} as no fluid type exists with that name", id, fullPath);
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

  /** Gets the still texture for the given fluid */
  public static ResourceLocation getFlowingTexture(FluidType fluid) {
    return getData(fluid).flowing();
  }

  /** Gets the still texture for the given fluid */
  @Nullable
  public static ResourceLocation getOverlayTexture(FluidType fluid) {
    return getData(fluid).overlay();
  }

  /** Gets the still texture for the given fluid */
  @Nullable
  public static ResourceLocation getCameraTexture(FluidType fluid) {
    return getData(fluid).camera();
  }

  /** Gets the still texture for the given fluid */
  public static int getColor(FluidType fluid) {
    return getData(fluid).color();
  }
}

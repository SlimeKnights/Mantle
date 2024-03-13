package slimeknights.mantle.fluid.texture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Manager for handling fluid tooltips */
public class FluidTextureManager implements Consumer<TextureStitchEvent.Pre> {
  /** Folder containing the logic */
  public static final String FOLDER = "mantle/fluid_texture";

  private static final int FOLDER_LENGTH = FOLDER.length() + 1;
  private static final int EXTENSION_LENGTH = ".json".length();

  /* Instance data */
  private static final FluidTextureManager INSTANCE = new FluidTextureManager();
  /** Map of fluid type to texture */
  private Map<FluidType,FluidTexture> textures = Collections.emptyMap();
  /** Fallback texture instance */
  private static final FluidTexture FALLBACK = new FluidTexture(new ResourceLocation("block/water_still"), new ResourceLocation("block/water_flow"), null, null, -1);

  /**
   * Initializes this manager, registering it with the resource manager
   * @param manager  Manager
   */
  public static void init(RegisterClientReloadListenersEvent manager) {
    MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TextureStitchEvent.Pre.class, INSTANCE);
  }

  @Override
  public void accept(TextureStitchEvent.Pre event) {
    if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
      // first, load in all fluid texture files, done in this event as otherwise we cannot guarantee it happens before the atlas stitches
      Map<FluidType, FluidTexture> map = new HashMap<>();

      ResourceManager manager = Minecraft.getInstance().getResourceManager();
      IForgeRegistry<FluidType> fluidTypeRegistry = ForgeRegistries.FLUID_TYPES.get();
      for (Map.Entry<ResourceLocation,Resource> entry : manager.listResources(FOLDER, location -> location.getPath().endsWith(".json")).entrySet()) {
        ResourceLocation fullPath = entry.getKey();
        String path = fullPath.getPath();
        ResourceLocation id = new ResourceLocation(fullPath.getNamespace(), path.substring(FOLDER_LENGTH, path.length() - EXTENSION_LENGTH));
        try (Reader reader = entry.getValue().openAsReader()) {
          // first step is to find the matching fluid type, if there is none ignore the file
          FluidType type = fluidTypeRegistry.getValue(id);
          if (type == null || !id.equals(fluidTypeRegistry.getKey(type))) {
            Mantle.logger.debug("Ignoring fluid texture {} from {} as no fluid type exists with that name", id, fullPath);
          } else {
            // next step is to read in the JSON from the file
            JsonObject json = GsonHelper.fromJson(JsonHelper.DEFAULT_GSON, reader, JsonObject.class);
            if (json == null) {
              Mantle.logger.warn("Couldn't load fluid texture file {} from {} as it's null or empty", id, fullPath);
            } else {
              // finally, parse it
              map.put(type, FluidTexture.deserialize(json));
            }
          }
        } catch (IllegalArgumentException | IOException | JsonParseException e) {
          Mantle.logger.error("Couldn't parse fluid texture {} from {}", id, fullPath, e);
        }
      }
      this.textures = map;

      // next, register all found textures with the atlas
      for (FluidTexture texture : map.values()) {
        event.addSprite(texture.still());
        event.addSprite(texture.flowing());
        ResourceLocation overlay = texture.overlay();
        if (overlay != null) {
          event.addSprite(overlay);
        }
        // no registering camera as its not stitched, its just drawn directly
      }
    }
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

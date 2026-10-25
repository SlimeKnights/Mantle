package slimeknights.mantle.client.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.listener.ISafeManagerReloadListener;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manager for texture map JSON, used in {@link NBTKeyModel}. JSON format:
 * <pre>
 * {
    "textures": {
      "name": "mod:item/name"
    }
  }
 * </pre>
 */
public class TextureMapManager implements ISafeManagerReloadListener {
  /** Loadable for the map of textures locations in the file */
  private static final Loadable<Map<String,ResourceLocation>> TEXTURE_MAP = StringLoadable.DEFAULT.mapWithValues(ResourceLocationLoadable.DEFAULT);
  /** Singleton instance of this loader */
  public static final TextureMapManager INSTANCE = new TextureMapManager();
  /** Folder holding the texture maps. */
  public static final String FOLDER = "mantle/textures";

  /** Texture map cache to avoid parsing the same map multiple times if reused. */
  private final Map<ResourceLocation, Map<String,ResourceLocation>> cache = new HashMap<>();

  private TextureMapManager() {}

  @Override
  public void onReloadSafe(ResourceManager manager) {
    // models bake in the first stage, so clear in the second stage when they are no longer in use
    cache.clear();
  }

  /** Gets a permutation list from the cache, loading it if missing */
  public Map<String,ResourceLocation> getTextureMap(ResourceManager manager, ResourceLocation location) {
    Map<String,ResourceLocation> map = cache.get(location);
    if (map == null) {
      // linked hash set ensures uniqueness while preserving order
      Map<String,ResourceLocation> values = new HashMap<>();
      // read from each resource pack, this is loosely based on tag loading
      for (Resource resource : manager.getResourceStack(location.withPath(FOLDER + '/' + location.getPath() +  ".json"))) {
        try (Reader reader = resource.openAsReader()) {
          // fetch root json
          JsonObject json = GsonHelper.convertToJsonObject(JsonParser.parseReader(reader), location.toString());
          // add all new entries
          values.putAll(TEXTURE_MAP.getIfPresent(json, "textures"));
        } catch (Exception ex) {
          Mantle.logger.error("Couldn't parse texture list {} in resource pack {}", location, resource.sourcePackId(), ex);
        }
      }
      // remove null entries, which can be used to clear out a lower level texture
      values.values().removeIf(Objects::isNull);
      map = Map.copyOf(values);
      cache.put(location, map);
    }
    return map;
  }
}

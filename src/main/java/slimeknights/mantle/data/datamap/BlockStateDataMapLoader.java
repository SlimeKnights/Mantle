package slimeknights.mantle.data.datamap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Generic JSON serializer which reads data from a block state style file for a mapping from block state to data.
 * @param <T>  Type of data
 */
public class BlockStateDataMapLoader<T> extends SimpleJsonResourceReloadListener {
  private final String name;
  @Getter
  private final String folder;
  @Getter
  private final Loadable<T> dataLoader;
  /** Map of data */
  private Map<BlockState,T> dataMap = Map.of();

  public BlockStateDataMapLoader(String name, String folder, Loadable<T> dataLoader) {
    super(JsonHelper.DEFAULT_GSON, folder);
    this.name = name;
    this.folder = folder;
    this.dataLoader = dataLoader;
  }

  /** Creates the parsing context for this loader */
  protected Loadable<T> prepareLoader(Map<ResourceLocation,JsonElement> jsons) {
    return dataLoader;
  }

  @Override
  protected void apply(Map<ResourceLocation,JsonElement> jsons, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
    long time = System.nanoTime();
    // final map being parsed
    Map<BlockState,T> dataMap = new HashMap<>();
    // temporary map to ensure we don't store partial value list
    Map<BlockState,T> localMap = new HashMap<>();
    // map of parsed data from entries fetching other data
    Map<ResourceLocation,T> locationMap = new HashMap<>();

    Loadable<T> dataLoader = prepareLoader(jsons);

    // parse through block registry, don't care about non-block entries
    for (Entry<ResourceKey<Block>,Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
      ResourceLocation location = entry.getKey().location();
      JsonElement element = jsons.get(location);
      if (element != null) {
        try {
          JsonObject json = GsonHelper.convertToJsonObject(element, location.toString());
          // skip empty objects, its the way to disable it
          if (json.keySet().isEmpty()) {
            continue;
          }
          JsonObject variants = GsonHelper.getAsJsonObject(json, "variants");
          StateDefinition<Block,BlockState> container = entry.getValue().getStateDefinition();
          List<BlockState> validStates = container.getPossibleStates();

          // for each variant, add in data values to the map
          for (Entry<String, JsonElement> variant : variants.entrySet()) {
            JsonElement variantElement = variant.getValue();
            String key = variant.getKey();
            // if its a string, treat it as a location to another JSON
            T data;
            if (variantElement.isJsonPrimitive()) {
              ResourceLocation parent = JsonHelper.convertToResourceLocation(variantElement, key);
              data = locationMap.get(parent);
              if (data == null) {
                JsonElement parentElement = jsons.get(parent);
                if (parentElement == null) {
                  throw new JsonSyntaxException("Missing parent at " + parent + " for " + name + ", used in " + location);
                }
                data = dataLoader.convert(parentElement, parent.toString());
                locationMap.put(parent, data);
              }
            } else {
              // otherwise parse the object directly
              data = dataLoader.convert(variantElement, key);
            }
            // upload the value into the map
            T effectivelyFinal = data;
            validStates.stream()
                       .filter(StateVariantStringBuilder.predicate(container, variant.getKey()))
                       .forEach(state -> localMap.put(state, effectivelyFinal));
          }
          // add all entries to the final map
          dataMap.putAll(localMap);
          localMap.clear();
        } catch (Exception e) {
          Mantle.logger.error("Failed to parse {} data for {}", name, location, e);
        }
      }
    }
    // update the datamap
    this.dataMap = dataMap;
    Mantle.logger.info("Finished loading {} {} in {} ms", dataMap.size(), name, (System.nanoTime() - time) / 1000000f);
  }

  /** Gets data from the given map, returning null if its missing */
  @Nullable
  public T get(BlockState state) {
    return dataMap.get(state);
  }

  /** Gets data from the given map, returning the default if its missing */
  public T get(BlockState state, T defaultValue) {
    return dataMap.getOrDefault(state, defaultValue);
  }
}

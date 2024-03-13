package slimeknights.mantle.data.listener;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Alternative to {@link net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener} that merges all json into a single builder rather than taking the top most JSON.
 * @param <B>  Builder class
 */
@RequiredArgsConstructor
@Log4j2
public abstract class MergingJsonDataLoader<B> implements ResourceManagerReloadListener {
  private static final int JSON_LENGTH = ".json".length();

  @VisibleForTesting
  protected final Gson gson;
  @VisibleForTesting
  protected final String folder;
  @VisibleForTesting
  protected final Function<ResourceLocation,B> builderConstructor;

  /**
   * Parses a particular JSON into the builder
   * @param builder   Builder object
   * @param id        ID of json being parsed
   * @param element   JSON data
   * @throws JsonSyntaxException  If the json failed to parse
   */
  protected abstract void parse(B builder, ResourceLocation id, JsonElement element) throws JsonSyntaxException;

  /**
   * Called when the JSON finished parsing to handle the final map
   * @param map      Map of data
   * @param manager  Resource manager
   */
  protected abstract void finishLoad(Map<ResourceLocation,B> map, ResourceManager manager);

  @Override
  public void onResourceManagerReload(ResourceManager manager) {
    Map<ResourceLocation,B> map = new HashMap<>();
    for (Entry<ResourceLocation,List<Resource>> entry : manager.listResourceStacks(folder, fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
      ResourceLocation filePath = entry.getKey();
      String path = filePath.getPath();
      ResourceLocation id = new ResourceLocation(filePath.getNamespace(), path.substring(folder.length() + 1, path.length() - JSON_LENGTH));

      for (Resource resource : entry.getValue()) {
        try (Reader reader = resource.openAsReader()) {
          JsonElement json = GsonHelper.fromJson(gson, reader, JsonElement.class);
          if (json == null) {
            log.error("Couldn't load data file {} from {} in data pack {} as its null or empty", id, filePath, resource.sourcePackId());
          } else {
            B builder = map.computeIfAbsent(id, builderConstructor);
            parse(builder, id, json);
          }
        } catch (RuntimeException | IOException ex) {
          log.error("Couldn't parse data file {} from {} in data pack {}", id, filePath, resource.sourcePackId(), ex);
        }
      }
    }
    finishLoad(map, manager);
  }
}

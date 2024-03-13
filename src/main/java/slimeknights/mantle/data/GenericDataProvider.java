package slimeknights.mantle.data;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import slimeknights.mantle.util.JsonHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Generic logic to convert any serializable object into JSON. */
@RequiredArgsConstructor
@Log4j2
public abstract class GenericDataProvider implements DataProvider {
  protected final DataGenerator generator;
  private final PackType type;
  private final String folder;
  private final Gson gson;

  public GenericDataProvider(DataGenerator generator, PackType type, String folder) {
    this(generator, type, folder, JsonHelper.DEFAULT_GSON);
  }

  /**
   * Saves the given object to JSON
   * @param output     Output for writing
   * @param location   Location relative to this data provider's root
   * @param object     Object to save, will be converted using this provider's GSON instance
   */
  protected void saveJson(CachedOutput output, ResourceLocation location, Object object) {
    try {
      Path path = this.generator.getOutputFolder().resolve(Paths.get(type.getDirectory(), location.getNamespace(), folder, location.getPath() + ".json"));
      DataProvider.saveStable(output, gson.toJsonTree(object), path);
    } catch (IOException e) {
      log.error("Couldn't create data for {}", location, e);
    }
  }
}

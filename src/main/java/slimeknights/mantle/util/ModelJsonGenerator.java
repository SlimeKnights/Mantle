package slimeknights.mantle.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.apache.commons.io.IOUtils;
import slimeknights.mantle.common.IGeneratedJson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModelJsonGenerator implements IDataProvider {

  private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
  private final DataGenerator generator;
  private final String modId;

  public ModelJsonGenerator(DataGenerator generatorIn, String modId) {
    this.generator = generatorIn;
    this.modId = modId;
  }

  @Override
  public void act(DirectoryCache cache) throws IOException {
    JsonObject blockObject = new JsonObject();
    JsonObject itemObject = new JsonObject();
    List<ResourceLocation> resourceLocations = new ArrayList<ResourceLocation>();
    int generatedEntries = 0;
    JsonObject modelCache = new JsonObject();

    Path cacheP = this.generator.getOutputFolder().resolve("cache/" + this.modId + "/models/models.json");

    if (Files.exists(cacheP)) {
      String jsonTxt = IOUtils.toString(cacheP.toUri(), "UTF-8");
      modelCache = new JsonParser().parse(jsonTxt).getAsJsonObject();
    }

    for (Block block : Registry.BLOCK) {
      blockObject = new JsonObject();
      ResourceLocation resourcelocation = Registry.BLOCK.getKey(block);

      if (!resourcelocation.getNamespace().equals(this.modId)) {
        continue;
      }
      if (!(block instanceof IGeneratedJson)) {
        continue;
      }
      if (modelCache.has(resourcelocation.toString())) {
        continue;
      }

      IGeneratedJson block1 = (IGeneratedJson) block;

      resourceLocations.add(resourcelocation);

      blockObject.addProperty("parent", block1.getParentToUse());

      blockObject.add("textures", block1.getTexturesToUse());

      modelCache.addProperty(resourcelocation.toString(), "UNUSED");
      generatedEntries++;

      Path path = this.generator.getOutputFolder().resolve("assets/" + this.modId + "/models/block/" + resourcelocation.getPath() + ".json");
      IDataProvider.func_218426_a(GSON, cache, blockObject, path);
    }

    for (Item item : Registry.ITEM) {
      itemObject = new JsonObject();
      ResourceLocation resourcelocation = Registry.ITEM.getKey(item);

      if (!resourcelocation.getNamespace().equals(this.modId)) {
        continue;
      }
      if (modelCache.has(resourcelocation.toString())) {
        continue;
      }

      if (resourceLocations.contains(resourcelocation)) {
        itemObject.addProperty("parent", resourcelocation.getNamespace() + ":block/" + resourcelocation.getPath());

        Path path = this.generator.getOutputFolder().resolve("assets/" + this.modId + "/models/item/" + resourcelocation.getPath() + ".json");
        IDataProvider.func_218426_a(GSON, cache, itemObject, path);
      }
      else {
        if (!(item instanceof IGeneratedJson)) {
          continue;
        }

        IGeneratedJson item1 = (IGeneratedJson) item;

        itemObject.addProperty("parent", item1.getParentToUse());

        itemObject.add("textures", item1.getTexturesToUse());

        modelCache.addProperty(resourcelocation.toString(), "UNUSED");
        generatedEntries++;

        Path path = this.generator.getOutputFolder().resolve("assets/" + this.modId + "/models/item/" + resourcelocation.getPath() + ".json");
        IDataProvider.func_218426_a(GSON, cache, itemObject, path);
      }
    }

    if (generatedEntries != 0) {
      IDataProvider.func_218426_a(GSON, cache, modelCache, cacheP);
    }

    cache.func_218456_c(cacheP);
  }

  /**
   * Gets a name for this provider, to use in logging.
   */
  @Override
  public String getName() {
    return "Model Generator";
  }
}

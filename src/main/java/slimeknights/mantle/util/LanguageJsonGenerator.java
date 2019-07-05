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

import java.io.IOException;
import java.nio.file.Path;

public class LanguageJsonGenerator implements IDataProvider {

  private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
  private final DataGenerator generator;
  private final String modId;

  public LanguageJsonGenerator(DataGenerator generatorIn, String modId) {
    this.generator = generatorIn;
    this.modId = modId;
  }

  @Override
  public void act(DirectoryCache cache) throws IOException {
    JsonObject names = new JsonObject();
    int generatedEntries = 0;

    Path cacheP = this.generator.getOutputFolder().resolve("cache/" + this.modId + "/lang/en_us.json");
    String jsonTxt = IOUtils.toString(cacheP.toUri(), "UTF-8");
    JsonObject langCache = new JsonParser().parse(jsonTxt).getAsJsonObject();

    for (Block block : Registry.BLOCK) {
      ResourceLocation resourcelocation = Registry.BLOCK.getKey(block);

      if (!resourcelocation.getNamespace().equals(this.modId)) {
        continue;
      }

      if (langCache.has(block.getTranslationKey())) {
        continue;
      }

      names.addProperty(block.getTranslationKey(), "RENAME");
      langCache.addProperty(block.getTranslationKey(), "RENAME");
      generatedEntries++;
    }

    for (Item item : Registry.ITEM) {
      ResourceLocation resourcelocation = Registry.ITEM.getKey(item);

      if (!resourcelocation.getNamespace().equals(this.modId)) {
        continue;
      }

      if (langCache.has(item.getTranslationKey())) {
        continue;
      }

      names.addProperty(item.getTranslationKey(), "RENAME");
      langCache.addProperty(item.getTranslationKey(), "RENAME");
      generatedEntries++;
    }

    Path path = this.generator.getOutputFolder().resolve("assets/" + this.modId + "/lang/en_us.json");
    IDataProvider.func_218426_a(GSON, cache, names, path);

    if (generatedEntries != 0) {
      IDataProvider.func_218426_a(GSON, cache, langCache, cacheP);
    }

    cache.func_218456_c(cacheP);
  }

  /**
   * Gets a name for this provider, to use in logging.
   */
  @Override
  public String getName() {
    return "Language Generator";
  }
}

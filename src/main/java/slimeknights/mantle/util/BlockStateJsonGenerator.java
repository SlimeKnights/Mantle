package slimeknights.mantle.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import slimeknights.mantle.common.IGeneratedJson;

import java.io.IOException;
import java.nio.file.Path;

public class BlockStateJsonGenerator implements IDataProvider {

  private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
  private final DataGenerator generator;
  private final String modId;

  public BlockStateJsonGenerator(DataGenerator generatorIn, String modId) {
    this.generator = generatorIn;
    this.modId = modId;
  }

  @Override
  public void act(DirectoryCache cache) throws IOException {
    JsonObject blockState = new JsonObject();

    for (Block block : Registry.BLOCK) {
      blockState = new JsonObject();
      ResourceLocation resourcelocation = Registry.BLOCK.getKey(block);

      if (!resourcelocation.getNamespace().equals(this.modId)) {
        continue;
      }

      if (!(block instanceof IGeneratedJson)) {
        continue;
      }

      IGeneratedJson block1 = (IGeneratedJson) block;

      blockState.add("variants", block1.getVariants());

      Path path = this.generator.getOutputFolder().resolve("assets/" + this.modId + "/blockstates/" + resourcelocation.getPath() + ".json");
      IDataProvider.func_218426_a(GSON, cache, blockState, path);
    }
  }

  /**
   * Gets a name for this provider, to use in logging.
   */
  @Override
  public String getName() {
    return "Blockstate Generator";
  }
}

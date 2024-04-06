package slimeknights.mantle.data;

import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/** Generic logic to convert any serializable object into JSON. */
@RequiredArgsConstructor
public abstract class GenericDataProvider implements DataProvider {
  protected final PackOutput.PathProvider pathProvider;
  private final Gson gson;

  public GenericDataProvider(PackOutput output, Target type, String folder, Gson gson) {
    this(output.createPathProvider(type, folder), gson);
  }

  public GenericDataProvider(DataGenerator generator, Target type, String folder, Gson gson) {
    this(generator.getPackOutput(), type, folder, gson);
  }

  public GenericDataProvider(PackOutput output, Target type, String folder) {
    this(output, type, folder, JsonHelper.DEFAULT_GSON);
  }

  public GenericDataProvider(DataGenerator generator, Target type, String folder) {
    this(generator, type, folder, JsonHelper.DEFAULT_GSON);
  }

  /**
   * Saves the given object to JSON
   * @param output     Output for writing
   * @param location   Location relative to this data provider's root
   * @param object     Object to save, will be converted using this provider's GSON instance
   */
  protected CompletableFuture<?> saveJson(CachedOutput output, ResourceLocation location, Object object) {
    return DataProvider.saveStable(output, gson.toJsonTree(object), this.pathProvider.json(location));
  }

  /**
   * Saves the given object to JSON using a codec
   * @param output     Output for writing
   * @param location   Location relative to this data provider's root
   * @param codec      Codec to save the object
   * @param object     Object to save, will be converted using the passed codec
   */
  protected <T> CompletableFuture<?> saveJson(CachedOutput output, ResourceLocation location, Codec<T> codec, T object) {
    return saveJson(output, location, codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow(false, Mantle.logger::error));
  }

  /** Combines a stream of completable futures into a single completable future */
  protected CompletableFuture<?> allOf(Stream<CompletableFuture<?>> stream) {
    return CompletableFuture.allOf(stream.toArray(CompletableFuture[]::new));
  }
}

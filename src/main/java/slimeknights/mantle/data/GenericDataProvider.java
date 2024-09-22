package slimeknights.mantle.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.RequiredArgsConstructor;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
  protected CompletableFuture<?> saveJson(CachedOutput output, ResourceLocation location, Object object, @Nullable Comparator<String> keyComparator) {
    return saveStable(output, gson.toJsonTree(object), this.pathProvider.json(location), keyComparator).exceptionally(e -> {
      Mantle.logger.error("Couldn't create data for {}", location, e);
      return null;
    });
  }

  /**
   * Saves the given object to JSON
   * @param output     Output for writing
   * @param location   Location relative to this data provider's root
   * @param object     Object to save, will be converted using this provider's GSON instance
   */
  protected CompletableFuture<?> saveJson(CachedOutput output, ResourceLocation location, Object object) {
    return saveJson(output, location, object, DataProvider.KEY_COMPARATOR);
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

  /** Recreation of {@link DataProvider#saveStable(CachedOutput, JsonElement, Path)} that allows swapping tke key comparator */
  @SuppressWarnings("UnstableApiUsage")
  static CompletableFuture<?> saveStable(CachedOutput cache, JsonElement pJson, Path pPath, @Nullable Comparator<String> keyComparator) {
    return CompletableFuture.runAsync(() -> {
      try {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        HashingOutputStream hashingOutput = new HashingOutputStream(Hashing.sha1(), byteOutput);

        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(hashingOutput, StandardCharsets.UTF_8))) {
          writer.setSerializeNulls(false);
          writer.setIndent("  ");
          GsonHelper.writeValue(writer, pJson, keyComparator);
        }
        cache.writeIfNeeded(pPath, byteOutput.toByteArray(), hashingOutput.hash());
      } catch (IOException exception) {
        throw new CompletionException(exception);
      }
    }, Util.backgroundExecutor());
  }
}

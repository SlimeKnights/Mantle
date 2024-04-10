package slimeknights.mantle.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/** Generic logic to convert any serializable object into JSON. */
@SuppressWarnings({"unused", "SameParameterValue"})  // API
@RequiredArgsConstructor
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
   * @param output         Output for writing
   * @param location       Location relative to this data provider's root
   * @param object         Object to save, will be converted using this provider's GSON instance
   * @param keyComparator  Key comparator to use
   */
  protected void saveJson(CachedOutput output, ResourceLocation location, Object object, @Nullable Comparator<String> keyComparator) {
    try {
      Path path = this.generator.getOutputFolder().resolve(Paths.get(type.getDirectory(), location.getNamespace(), folder, location.getPath() + ".json"));
      saveStable(output, gson.toJsonTree(object), path, keyComparator);
    } catch (IOException e) {
      Mantle.logger.error("Couldn't create data for {}", location, e);
    }
  }

  /**
   * Saves the given object to JSON
   * @param output     Output for writing
   * @param location   Location relative to this data provider's root
   * @param object     Object to save, will be converted using this provider's GSON instance
   */
  protected void saveJson(CachedOutput output, ResourceLocation location, Object object) {
    saveJson(output, location, object, DataProvider.KEY_COMPARATOR);
  }

  /**
   * Saves the given object to JSON using a codec
   * @param output     Output for writing
   * @param location   Location relative to this data provider's root
   * @param codec      Codec to save the object
   * @param object     Object to save, will be converted using the passed codec
   */
  protected <T> void saveJson(CachedOutput output, ResourceLocation location, Codec<T> codec, T object) {
    saveJson(output, location, codec.encodeStart(JsonOps.INSTANCE, object).getOrThrow(false, Mantle.logger::error));
  }

  /** Recreation of {@link DataProvider#saveStable(CachedOutput, JsonElement, Path)} that allows swapping tke key comparator */
  @SuppressWarnings("UnstableApiUsage")
  static void saveStable(CachedOutput cache, JsonElement pJson, Path pPath, @Nullable Comparator<String> keyComparator) throws IOException {
    ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
    HashingOutputStream hashingOutput = new HashingOutputStream(Hashing.sha1(), byteOutput);
    JsonWriter writer = new JsonWriter(new OutputStreamWriter(hashingOutput, StandardCharsets.UTF_8));
    writer.setSerializeNulls(false);
    writer.setIndent("  ");
    GsonHelper.writeValue(writer, pJson, keyComparator);
    writer.close();
    cache.writeIfNeeded(pPath, byteOutput.toByteArray(), hashingOutput.hash());
  }
}

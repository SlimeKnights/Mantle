package slimeknights.mantle.data;

import com.google.gson.Gson;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.PathProvider;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

/** Extension of {@link GenericDataProvider} for objects using {@link Gson} to serialize. */
public abstract class GsonDataProvider extends GenericDataProvider {
  private final Gson gson;

  public GsonDataProvider(PathProvider pathProvider, Gson gson) {
    super(pathProvider);
    this.gson = gson;
  }

  public GsonDataProvider(PackOutput output, Target type, String folder, Gson gson) {
    super(output, type, folder);
    this.gson = gson;
  }

  /**
   * Saves the given object to JSON using this provider's GSON instance.
   * @param output     Output for writing
   * @param location   Location relative to this data provider's root
   * @param object     Object to save, will be converted using this provider's GSON instance
   */
  @SuppressWarnings("SameParameterValue")  // API
  protected CompletableFuture<?> saveGson(CachedOutput output, ResourceLocation location, Object object, @Nullable Comparator<String> keyComparator) {
    return saveJson(output, location, gson.toJsonTree(object), keyComparator);
  }

  /**
   * Saves the given object to JSON using this provider's GSON instance.
   * @param output     Output for writing
   * @param location   Location relative to this data provider's root
   * @param object     Object to save, will be converted using this provider's GSON instance
   */
  protected CompletableFuture<?> saveGson(CachedOutput output, ResourceLocation location, Object object) {
    return saveGson(output, location, object, DataProvider.KEY_COMPARATOR);
  }
}

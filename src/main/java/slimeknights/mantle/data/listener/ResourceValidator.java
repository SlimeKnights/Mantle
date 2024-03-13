package slimeknights.mantle.data.listener;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/** Utility that handles checking if a resource exists in any resource pack. */
@SuppressWarnings("unused")  // API
public class ResourceValidator implements IEarlySafeManagerReloadListener, Predicate<ResourceLocation> {
  private final String folder;
  private final int trim;
  private final String extension;
  protected Set<ResourceLocation> resources;

  /**
   * Gets a resource validator instance
   * @param folder     Folder to search
   * @param trim       Text to trim off resource locations
   * @param extension  File extension
   */
  public ResourceValidator(String folder, String trim, String extension) {
    this.folder = folder;
    this.trim = trim.length() + 1;
    this.extension = extension;
    this.resources = ImmutableSet.of();
  }

  @Override
  public void onReloadSafe(ResourceManager manager) {
    int extensionLength = extension.length();
    this.resources = manager.listResources(folder, (loc) -> {
      // must have proper extension and contain valid characters
      return loc.getPath().endsWith(extension);
    }).keySet().stream().map((location) -> {
      String path = location.getPath();
      return new ResourceLocation(location.getNamespace(), path.substring(trim, path.length() - extensionLength));
    }).collect(Collectors.toSet());
  }

  @Override
  public boolean test(ResourceLocation location) {
    return resources.contains(location);
  }

  /**
   * Clears the resource cache, saves RAM as there could be a lot of locations
   */
  public void clear() {
    resources = ImmutableSet.of();
  }
}

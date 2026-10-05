package slimeknights.mantle.client.book.repository;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import slimeknights.mantle.client.book.data.SectionData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public abstract class BookRepository {

  @SuppressWarnings("StaticInitializerReferencesSubClass") // will only occur in very specific threaded environment
  public static final BookRepository DUMMY = new DummyRepository();

  /** Language a book falls back to when the selected language is missing a file */
  public static final String DEFAULT_LANGUAGE = "en_us";

  /** Gets the language selected in the client options, or {@link #DEFAULT_LANGUAGE} if there is none yet */
  public static String getSelectedLanguage() {
    //noinspection ConstantConditions - this was proven to be null once
    if (Minecraft.getInstance().getLanguageManager() != null) {
      String selected = Minecraft.getInstance().getLanguageManager().getSelected();
      //noinspection ConstantConditions - see above
      if (selected != null) {
        return selected;
      }
    }
    return DEFAULT_LANGUAGE;
  }

  public abstract List<SectionData> getSections();

  @Nullable
  public ResourceLocation getResourceLocation(@Nullable String path) {
    return this.getResourceLocation(path, false);
  }

  @Nullable
  public abstract ResourceLocation getResourceLocation(@Nullable String path, boolean safe);

  /**
   * Gets the location of the given path in a specific language, ignoring the selected language.
   * Used to load the English translations as a fallback for keys missing in the selected language.
   * @param path      Path to find, relative to the repository
   * @param language  Language to look in
   * @return  Location of the file, or null if the repository has no copy of it in that language
   */
  @Nullable
  public ResourceLocation getLanguageResourceLocation(@Nullable String path, String language) {
    return null;
  }

  /** Gets a resource from the given location */
  public abstract Optional<Resource> getLocation(@Nullable ResourceLocation loc);

  /** Gets a resource from the given location, returning null if it does not exist */
  @Nullable
  public Resource getResource(@Nullable ResourceLocation loc) {
    return getLocation(loc).orElse(null);
  }

  /** Checks if the given resource exists */
  @SuppressWarnings("unused") // API
  public boolean resourceExists(@Nullable String location) {
    if(location == null) {
      return false;
    }

    return this.resourceExists(new ResourceLocation(location));
  }

  /** Checks if the given resource exists */
  public boolean resourceExists(@Nullable ResourceLocation location) {
    return getLocation(location).isPresent();
  }

  public String resourceToString(@Nullable Resource resource) {
    return this.resourceToString(resource, true);
  }

  public abstract String resourceToString(@Nullable Resource resource, boolean skipComments);
}

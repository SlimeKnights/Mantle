package slimeknights.mantle.client.book.repository;

import net.minecraft.server.packs.resources.Resource;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import slimeknights.mantle.client.book.data.SectionData;

import javax.annotation.Nullable;

public abstract class BookRepository {

  @SuppressWarnings("StaticInitializerReferencesSubClass") // will only occur in very specific threaded environment
  public static final BookRepository DUMMY = new DummyRepository();

  public abstract List<SectionData> getSections();

  @Nullable
  public ResourceLocation getResourceLocation(@Nullable String path) {
    return this.getResourceLocation(path, false);
  }

  @Nullable
  public abstract ResourceLocation getResourceLocation(@Nullable String path, boolean safe);

  @Nullable
  public abstract Resource getResource(@Nullable ResourceLocation loc);

  @SuppressWarnings("unused") // API
  public boolean resourceExists(@Nullable String location) {
    if(location == null) {
      return false;
    }

    return this.resourceExists(new ResourceLocation(location));
  }

  public abstract boolean resourceExists(@Nullable ResourceLocation location);

  public String resourceToString(@Nullable Resource resource) {
    return this.resourceToString(resource, true);
  }

  public abstract String resourceToString(@Nullable Resource resource, boolean skipComments);
}

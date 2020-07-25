package slimeknights.mantle.client.book.repository;

import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

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
  public abstract IResource getResource(@Nullable ResourceLocation loc);

  @SuppressWarnings("unused") // API
  public boolean resourceExists(@Nullable String location) {
    if(location == null) {
      return false;
    }

    return this.resourceExists(new ResourceLocation(location));
  }

  public abstract boolean resourceExists(@Nullable ResourceLocation location);

  public String resourceToString(@Nullable IResource resource) {
    return this.resourceToString(resource, true);
  }

  public abstract String resourceToString(@Nullable IResource resource, boolean skipCommments);
}

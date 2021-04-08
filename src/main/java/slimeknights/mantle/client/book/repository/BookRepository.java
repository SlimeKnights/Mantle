package slimeknights.mantle.client.book.repository;

import java.util.List;

import slimeknights.mantle.client.book.data.SectionData;

import org.jetbrains.annotations.Nullable;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public abstract class BookRepository {

  @SuppressWarnings("StaticInitializerReferencesSubClass") // will only occur in very specific threaded environment
  public static final BookRepository DUMMY = new DummyRepository();

  public abstract List<SectionData> getSections();

  @Nullable
  public Identifier getResourceLocation(@Nullable String path) {
    return this.getResourceLocation(path, false);
  }

  @Nullable
  public abstract Identifier getResourceLocation(@Nullable String path, boolean safe);

  @Nullable
  public abstract Resource getResource(@Nullable Identifier loc);

  @SuppressWarnings("unused") // API
  public boolean resourceExists(@Nullable String location) {
    if(location == null) {
      return false;
    }

    return this.resourceExists(new Identifier(location));
  }

  public abstract boolean resourceExists(@Nullable Identifier location);

  public String resourceToString(@Nullable Resource resource) {
    return this.resourceToString(resource, true);
  }

  public abstract String resourceToString(@Nullable Resource resource, boolean skipCommments);
}

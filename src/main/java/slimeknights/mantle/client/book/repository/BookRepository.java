package slimeknights.mantle.client.book.repository;

import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import slimeknights.mantle.client.book.data.SectionData;

public abstract class BookRepository {

  public static final BookRepository DUMMY = new DummyRepository();

  public abstract List<SectionData> getSections();

  public ResourceLocation getResourceLocation(String path) {
    return getResourceLocation(path, false);
  }

  public abstract ResourceLocation getResourceLocation(String path, boolean safe);

  public abstract IResource getResource(ResourceLocation loc);

  public boolean resourceExists(String location) {
    return resourceExists(new ResourceLocation(location));
  }

  public abstract boolean resourceExists(ResourceLocation location);

  public String resourceToString(IResource resource) {
    return resourceToString(resource, true);
  }

  public abstract String resourceToString(IResource resource, boolean skipCommments);
}

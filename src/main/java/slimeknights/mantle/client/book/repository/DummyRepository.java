package slimeknights.mantle.client.book.repository;

import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.util.List;

import slimeknights.mantle.client.book.data.SectionData;

public class DummyRepository extends BookRepository {

  @Override
  public List<SectionData> getSections() {
    return null;
  }

  @Override
  public ResourceLocation getResourceLocation(String path, boolean safe) {
    return null;
  }

  @Override
  public IResource getResource(ResourceLocation loc) {
    return null;
  }

  @Override
  public boolean resourceExists(ResourceLocation location) {
    return false;
  }

  @Override
  public String resourceToString(IResource resource, boolean skipCommments) {
    return null;
  }
}

package slimeknights.mantle.client.book.repository;

import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import slimeknights.mantle.client.book.data.SectionData;

import java.util.Collections;
import java.util.List;

public class DummyRepository extends BookRepository {

  @Override
  public List<SectionData> getSections() {
    return Collections.emptyList();
  }

  @Override
  public ResourceLocation getResourceLocation(String path, boolean safe) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IResource getResource(ResourceLocation loc) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean resourceExists(ResourceLocation location) {
    return false;
  }

  @Override
  public String resourceToString(IResource resource, boolean skipCommments) {
    return "";
  }
}

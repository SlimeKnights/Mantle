package slimeknights.mantle.client.book.repository;

import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import slimeknights.mantle.client.book.data.SectionData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class DummyRepository extends BookRepository {

  @Override
  public List<SectionData> getSections() {
    return Collections.emptyList();
  }

  @Override
  public ResourceLocation getResourceLocation(@Nullable String path, boolean safe) {
    return null;
  }

  @Override
  public IResource getResource(@Nullable ResourceLocation loc) {
    return null;
  }

  @Override
  public boolean resourceExists(@Nullable ResourceLocation location) {
    return false;
  }

  @Override
  public String resourceToString(@Nullable IResource resource, boolean skipComments) {
    return "";
  }
}

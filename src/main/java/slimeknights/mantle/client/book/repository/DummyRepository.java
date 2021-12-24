package slimeknights.mantle.client.book.repository;

import net.minecraft.server.packs.resources.Resource;
import net.minecraft.resources.ResourceLocation;
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
  public Resource getResource(@Nullable ResourceLocation loc) {
    return null;
  }

  @Override
  public boolean resourceExists(@Nullable ResourceLocation location) {
    return false;
  }

  @Override
  public String resourceToString(@Nullable Resource resource, boolean skipComments) {
    return "";
  }
}

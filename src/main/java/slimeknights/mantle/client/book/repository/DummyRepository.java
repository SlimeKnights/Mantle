package slimeknights.mantle.client.book.repository;

import slimeknights.mantle.client.book.data.SectionData;

import javax.annotation.Nullable;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import java.util.Collections;
import java.util.List;

public class DummyRepository extends BookRepository {

  @Override
  public List<SectionData> getSections() {
    return Collections.emptyList();
  }

  @Override
  public Identifier getResourceLocation(@Nullable String path, boolean safe) {
    return null;
  }

  @Override
  public Resource getResource(@Nullable Identifier loc) {
    return null;
  }

  @Override
  public boolean resourceExists(@Nullable Identifier location) {
    return false;
  }

  @Override
  public String resourceToString(@Nullable Resource resource, boolean skipCommments) {
    return "";
  }
}

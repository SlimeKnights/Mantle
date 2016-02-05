package slimeknights.mantle.client.book.repository;

import java.util.List;
import slimeknights.mantle.client.book.data.SectionData;

public abstract class BookRepository {

  public final boolean hasAppearanceData;

  public BookRepository() {
    this(false);
  }

  public BookRepository(boolean hasAppearanceData) {
    this.hasAppearanceData = hasAppearanceData;
  }

  public abstract List<SectionData> getSections();
}

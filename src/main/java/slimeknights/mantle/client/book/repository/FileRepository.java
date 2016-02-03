package slimeknights.mantle.client.book.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.SectionData;
import static slimeknights.mantle.client.book.ResourceHelper.getResource;
import static slimeknights.mantle.client.book.ResourceHelper.getResourceLocation;
import static slimeknights.mantle.client.book.ResourceHelper.resourceToString;
import static slimeknights.mantle.client.book.ResourceHelper.setRoot;

public class FileRepository extends BookRepository {

  public final String location;

  public FileRepository(String location) {
    this(location, false);
  }

  public FileRepository(String location, boolean hasAppearanceData) {
    super(hasAppearanceData);

    this.location = location;
  }

  @Override
  public List<SectionData> getSections() {
    setRoot(location);
    return new ArrayList<>(Arrays.asList(BookLoader.GSON.fromJson(resourceToString(getResource(getResourceLocation("index.json"))), SectionData[].class)));
  }
}

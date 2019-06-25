package slimeknights.mantle.client.book.data;

import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.repository.ModuleFileRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SectionDataModule extends SectionData {

  /** Module required to load this section, see {@link slimeknights.mantle.pulsar.control.PulseManager#isPulseLoaded(String)} for more details */
  public String module = "";

  @Override
  protected ArrayList<PageData> getPages(String data) {
    // this should always be the case, but just for safety
    if (this.source instanceof ModuleFileRepository) {
      // just filters out pages where the module is not loaded
      Predicate<String> manager = ((ModuleFileRepository) this.source).getManager();
      return new ArrayList<>(Arrays.stream(BookLoader.GSON.fromJson(data, PageDataModule[].class))
              .filter((page) -> page.module.isEmpty() || manager.test(page.module))
              .collect(Collectors.toList()));
    }
    return super.getPages(data);
  }
}

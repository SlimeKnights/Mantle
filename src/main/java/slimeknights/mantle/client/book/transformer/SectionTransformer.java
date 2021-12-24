package slimeknights.mantle.client.book.transformer;

import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.client.book.data.PageData;
import slimeknights.mantle.client.book.data.SectionData;
import slimeknights.mantle.client.book.data.content.PageContent;

/** Transformer that locates a specific section to transform */
@RequiredArgsConstructor
public abstract class SectionTransformer extends BookTransformer {
  /** Name of the section to transform */
  protected final String sectionName;

  @Override
  public final void transform(BookData book) {
    SectionData data = null;
    for (SectionData section : book.sections) {
      if (sectionName.equals(section.name)) {
        data = section;
        break;
      }
    }

    if (data != null) {
      transform(book, data);
    }
  }

  /** Called when the section is found to apply the transformer */
  public abstract void transform(BookData book, SectionData section);

  /** Helper to add a page to the section */
  protected PageData addPage(SectionData data, String name, ResourceLocation type, PageContent content) {
    PageData page = new PageData(true);
    page.source = data.source;
    page.parent = data;
    page.name = name;
    page.type = type;
    page.content = content;
    page.load();

    data.pages.add(page);

    return page;
  }
}

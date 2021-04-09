package slimeknights.mantle.client.book.data.element;

import slimeknights.mantle.client.book.repository.BookRepository;

public interface IDataElement {

  void load(BookRepository source);
}

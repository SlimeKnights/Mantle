package slimeknights.mantle.util;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableListIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkPositionIndex;

public class ImmutableConcatList<E> implements List<E> {

  protected final FluentIterable<E> iterable;

  public ImmutableConcatList(List<E>... lists) {
    iterable = FluentIterable.from(Iterables.concat(lists));
  }

  public ImmutableConcatList(List<List<E>> lists) {
    iterable = FluentIterable.from(Iterables.concat(lists));
  }

  @Override
  public int size() {
    return iterable.size();
  }

  @Override
  public boolean isEmpty() {
    return iterable.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return iterable.contains(o);
  }

  @Override
  public Iterator<E> iterator() {
    return iterable.iterator();
  }

  @Override
  public Object[] toArray() {
    return iterable.toList().toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return iterable.toList().toArray(a);
  }

  @Override
  public boolean add(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object e : c)
      if (!contains(e))
        return false;
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public E get(int index) {
    return iterable.get(index);
  }

  @Override
  public E set(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void add(int index, E element) {
    throw new UnsupportedOperationException();
  }

  @Override
  public E remove(int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOf(final Object o) {
    Iterator<E> iterator = iterable.iterator();
    for (int i = 0; iterator.hasNext(); i++) {
      if(o == iterator.next()) {
        return i;
      }
    }

    return -1;
  }

  @Override
  public int lastIndexOf(Object o) {
    Iterator<E> iterator = iterable.iterator();
    int j = -1;
    for (int i = 0; iterator.hasNext(); i++) {
      if(o == iterator.next()) {
        j = i;
      }
    }

    return j;
  }

  @Override
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return new ListItr<E>(this.size(), index) {
      @Override
      protected E get(int index) {
        return ImmutableConcatList.this.get(index);
      }
    };
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  // basically a copy of AbstractIndexedListIterator
  private abstract static class ListItr<E> extends UnmodifiableListIterator<E> {

    private final int size;
    private int position;

    public ListItr(int size, int position) {
      checkPositionIndex(position, size);
      this.size = size;
      this.position = position;
    }

    protected abstract E get(int index);

    @Override
    public final boolean hasNext() {
      return position < size;
    }

    @Override
    public final E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return get(position++);
    }

    @Override
    public final int nextIndex() {
      return position;
    }

    @Override
    public final boolean hasPrevious() {
      return position > 0;
    }

    @Override
    public final E previous() {
      if (!hasPrevious()) {
        throw new NoSuchElementException();
      }
      return get(--position);
    }

    @Override
    public final int previousIndex() {
      return position - 1;
    }
  }
}

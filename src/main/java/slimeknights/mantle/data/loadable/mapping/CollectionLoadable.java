package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Shared base class for a loadable of a collection of elements */
@SuppressWarnings("unused") // API
@RequiredArgsConstructor
public abstract class CollectionLoadable<T,C extends Collection<T>> implements ArrayLoadable<C> {
  /** Loadable for an object */
  protected final Loadable<T> base;
  /** Minimum list size allowed */
  private final int minSize;

  /** Creates a new builder instance for the given expected size */
  protected Collection<T> createBuilder(int size) {
    return new ArrayList<>(size);
  }

  /** Builds the final collection, given the passed mutable collection. */
  protected abstract C build(Collection<T> builder);

  @Override
  public void checkSize(String key, int size, ErrorFactory error) {
    int minSize = this.minSize;
    if (minSize == COMPACT) {
      minSize = 1;
    }
    if (size < minSize) {
      throw error.create(key + " must have at least " + minSize + " elements");
    }
  }

  @Override
  public boolean allowCompact() {
    return minSize < 0;
  }

  @Override
  public int getLength(C array) {
    return array.size();
  }

  @Override
  public C convertCompact(JsonElement element, String key, TypedMap context) {
    return build(List.of(base.convert(element, key, context)));
  }

  @Override
  public C convertArray(JsonArray array, String key, TypedMap context) {
    Collection<T> builder = createBuilder(array.size());
    for (int i = 0; i < array.size(); i++) {
      builder.add(base.convert(array.get(i), key + '[' + i + ']', context));
    }
    return build(builder);
  }

  @Override
  public JsonElement serializeFirst(C collection) {
    return base.serialize(collection.iterator().next());
  }

  @Override
  public void serializeAll(JsonArray array, C collection) {
    for (T element : collection) {
      array.add(base.serialize(element));
    }
  }

  @Override
  public C decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    int max = buffer.readVarInt();
    Collection<T> builder = createBuilder(max);
    for (int i = 0; i < max; i++) {
      builder.add(base.decode(buffer, context));
    }
    return build(builder);
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, C collection) {
    buffer.writeVarInt(collection.size());
    for (T element : collection) {
      base.encode(buffer, element);
    }
  }
}

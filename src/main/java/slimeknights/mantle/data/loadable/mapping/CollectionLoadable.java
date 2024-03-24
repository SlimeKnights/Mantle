package slimeknights.mantle.data.loadable.mapping;

import com.google.common.collect.ImmutableCollection;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.Collection;

/** Shared base class for a loadable of a collection of elements */
@RequiredArgsConstructor
public abstract class CollectionLoadable<T,C extends Collection<T>,B extends ImmutableCollection.Builder<T>> implements Loadable<C> {
  /** Loadable for an object */
  private final Loadable<T> base;
  /** If true, empty is an allowed value */
  private final int minSize;

  /** Creates a builder for the collection */
  protected abstract B makeBuilder();

  /** Builds the final collection */
  protected abstract C build(B builder);

  @Override
  public C convert(JsonElement element, String key) throws JsonSyntaxException {
    JsonArray array = GsonHelper.convertToJsonArray(element, key);
    if (array.size() < minSize) {
      throw new JsonSyntaxException(key + " must have at least " + minSize + " elements");
    }
    B builder = makeBuilder();
    for (int i = 0; i < array.size(); i++) {
      builder.add(base.convert(array.get(i), key + '[' + i + ']'));
    }
    return build(builder);
  }

  @Override
  public JsonArray serialize(C collection) {
    JsonArray array = new JsonArray();
    for (T element : collection) {
      array.add(base.serialize(element));
    }
    return array;
  }

  @Override
  public C fromNetwork(FriendlyByteBuf buffer) {
    B builder = makeBuilder();
    int max = buffer.readVarInt();
    for (int i = 0; i < max; i++) {
      builder.add(base.fromNetwork(buffer));
    }
    return build(builder);
  }

  @Override
  public void toNetwork(C collection, FriendlyByteBuf buffer) {
    buffer.writeVarInt(collection.size());
    for (T element : collection) {
      base.toNetwork(element, buffer);
    }
  }
}

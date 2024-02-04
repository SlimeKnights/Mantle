package slimeknights.mantle.data.predicate;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.GenericLoaderRegistry;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.loader.NestedLoader;

/**
 * Predicate that inverts the condition.
 * Generally, this class should not be constructed directly, use {@link IJsonPredicate#inverted()} instead as it will simplify inverted forms.
 */
@RequiredArgsConstructor
public class InvertedJsonPredicate<I> implements IJsonPredicate<I> {
  private final Loader<I> loader;
  private final IJsonPredicate<I> base;

  @Override
  public boolean matches(I input) {
    return !base.matches(input);
  }

  @Override
  public IGenericLoader<? extends IJsonPredicate<I>> getLoader() {
    return loader;
  }

  @Override
  public IJsonPredicate<I> inverted() {
    return base;
  }

  /** Loader for an inverted JSON predicate */
  @RequiredArgsConstructor
  public static class Loader<I> implements IGenericLoader<InvertedJsonPredicate<I>> {
    /** Loader for predicates of this type */
    private final GenericLoaderRegistry<IJsonPredicate<I>> loader;
    /** If true, will support the nested method for predicates as a fallback, will still prefer the non-nested method for serializing */
    private final boolean allowNested;

    public Loader(GenericLoaderRegistry<IJsonPredicate<I>> loader) {
      this(loader, true);
    }

    /** Creates a new instance of an inverted predicate */
    public InvertedJsonPredicate<I> create(IJsonPredicate<I> predicate) {
      return new InvertedJsonPredicate<>(this, predicate);
    }

    @Override
    public InvertedJsonPredicate<I> deserialize(JsonObject json) {
      if (allowNested && json.has("predicate")) {
        return create(loader.getAndDeserialize(json, "predicate"));
      }
      NestedLoader.mapType(json, "inverted_type");
      return create(loader.deserialize(json));
    }

    @Override
    public InvertedJsonPredicate<I> fromNetwork(FriendlyByteBuf buffer) {
      return create(loader.fromNetwork(buffer));
    }

    @Override
    public void serialize(InvertedJsonPredicate<I> object, JsonObject json) {
      NestedLoader.serializeInto(json, "inverted_type", loader, object.base);
    }

    @Override
    public void toNetwork(InvertedJsonPredicate<I> object, FriendlyByteBuf buffer) {
      loader.toNetwork(object.base, buffer);
    }
  };
}

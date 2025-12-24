package slimeknights.mantle.data.predicate;

import lombok.RequiredArgsConstructor;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

/** Predicate registry that implements tag predicates */
public class TagPredicateRegistry<R,T> extends PredicateRegistry<T> {
  private final BiPredicate<TagKey<R>,T> tagMatcher;
  private final RecordLoadable<TagPredicate> tagLoader;

  /**
   * Creates a new registry for predicates that support tags.
   * @param name            Name to display in error messages
   * @param anyInstance  Any instance that always returns true. Will be used for nulls and missing fields.
   * @param noneInstance  None instance that always returns false. Used as the default for the conditional loader. If null, no none is registered.
   * @param tagKey          Loader for tag keys
   * @param tagMatcher      Logic to match a tag for the passed type
   */
  public TagPredicateRegistry(String name, IJsonPredicate<T> anyInstance, @Nullable IJsonPredicate<T> noneInstance, Loadable<TagKey<R>> tagKey, BiPredicate<TagKey<R>,T> tagMatcher) {
    super(name, anyInstance, noneInstance);
    this.tagMatcher = tagMatcher;
    this.tagLoader = RecordLoadable.create(tagKey.requiredField("tag", p -> p.tag), TagPredicate::new);
    this.register(Mantle.getResource("tag"), tagLoader);
  }

  /** @deprecated use {@link #TagPredicateRegistry(String, IJsonPredicate, IJsonPredicate, Loadable, BiPredicate)} */
  @Deprecated(forRemoval = true)
  public TagPredicateRegistry(String name, IJsonPredicate<T> anyInstance, Loadable<TagKey<R>> tagKey, BiPredicate<TagKey<R>,T> tagMatcher) {
    this(name, anyInstance, null, tagKey, tagMatcher);
  }

  /** Creates a new tag predicate */
  public IJsonPredicate<T> tag(TagKey<R> tag) {
    return new TagPredicate(tag);
  }

  /** Predicate matching values in a tag */
  @RequiredArgsConstructor
  private class TagPredicate implements IJsonPredicate<T> {
    private final TagKey<R> tag;

    @Override
    public boolean matches(T input) {
      return tagMatcher.test(tag, input);
    }

    @Override
    public IJsonPredicate<T> inverted() {
      return invert(this);
    }

    @Override
    public RecordLoadable<? extends IJsonPredicate<T>> getLoader() {
      return tagLoader;
    }
  }
}

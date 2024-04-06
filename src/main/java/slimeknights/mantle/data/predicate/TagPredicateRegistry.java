package slimeknights.mantle.data.predicate;

import lombok.RequiredArgsConstructor;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.function.BiPredicate;

/** Predicate registry that implements tag and set predicates */
public class TagPredicateRegistry<R,T> extends PredicateRegistry<T> {
  private final BiPredicate<TagKey<R>,T> tagMatcher;
  private final RecordLoadable<TagPredicate> tagLoader;

  /**
   * Creates a new instance
   * @param defaultInstance Default instance, typically expected to be an any predicate.
   * @param tagKey          Loader for tag keys
   * @param tagMatcher      Logic to match a tag for the passed type
   */
  public TagPredicateRegistry(String name, IJsonPredicate<T> defaultInstance, Loadable<TagKey<R>> tagKey, BiPredicate<TagKey<R>,T> tagMatcher) {
    super(name, defaultInstance);
    this.tagMatcher = tagMatcher;
    this.tagLoader = RecordLoadable.create(tagKey.requiredField("tag", p -> p.tag), TagPredicate::new);
    this.register(Mantle.getResource("tag"), tagLoader);
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
    public IGenericLoader<? extends IJsonPredicate<T>> getLoader() {
      return tagLoader;
    }
  }
}

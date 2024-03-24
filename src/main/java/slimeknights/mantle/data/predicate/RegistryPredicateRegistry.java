package slimeknights.mantle.data.predicate;

import lombok.RequiredArgsConstructor;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

/** Predicate registry that implements tag and set predicates */
public class RegistryPredicateRegistry<R,T> extends PredicateRegistry<T> {
  private final Function<T,R> getter;
  private final BiPredicate<TagKey<R>,T> tagMatcher;
  private final RecordLoadable<SetPredicate> setLoader;
  private final RecordLoadable<TagPredicate> tagLoader;

  /**
   * Creates a new instance
   * @param defaultInstance Default instance, typically expected to be an any predicate.
   * @param registry        Loading logic for the backing registry
   * @param getter          Method mapping from the predicate type to the registry type
   * @param setKey          Key to use for the set predicate
   * @param tagKey          Loader for tag keys
   * @param tagMatcher      Logic to match a tag for the passed type
   */
  public RegistryPredicateRegistry(String name, IJsonPredicate<T> defaultInstance, Loadable<R> registry, Function<T,R> getter, String setKey, Loadable<TagKey<R>> tagKey, BiPredicate<TagKey<R>,T> tagMatcher) {
    super(name, defaultInstance);
    // fields for loaders
    this.getter = getter;
    this.tagMatcher = tagMatcher;
    // create loaders
    this.setLoader = RecordLoadable.create(registry.set().requiredField(setKey, p -> p.set), SetPredicate::new);
    this.tagLoader = RecordLoadable.create(tagKey.requiredField("tag", p -> p.tag), TagPredicate::new);
    // register loaders
    this.register(Mantle.getResource("set"), setLoader);
    this.register(Mantle.getResource("tag"), tagLoader);
  }

  /** Creates a new set predicate given the passed values */
  public IJsonPredicate<T> setOf(Set<R> values) {
    return new SetPredicate(values);
  }

  /** Creates a new tag predicate */
  public IJsonPredicate<T> tag(TagKey<R> tag) {
    return new TagPredicate(tag);
  }


  /** Predicate matching an entry from a set of values */
  @RequiredArgsConstructor
  private class SetPredicate implements IJsonPredicate<T> {
    private final Set<R> set;

    @Override
    public boolean matches(T input) {
      return set.contains(getter.apply(input));
    }

    @Override
    public IJsonPredicate<T> inverted() {
      return invert(this);
    }

    @Override
    public IGenericLoader<? extends IJsonPredicate<T>> getLoader() {
      return setLoader;
    }
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

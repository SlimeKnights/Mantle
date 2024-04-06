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
public class RegistryPredicateRegistry<R,T> extends TagPredicateRegistry<R,T> {
  private final Function<T,R> getter;
  private final RecordLoadable<SetPredicate> setLoader;

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
    super(name, defaultInstance, tagKey, tagMatcher);
    this.getter = getter;
    this.setLoader = RecordLoadable.create(registry.set().requiredField(setKey, p -> p.set), SetPredicate::new);
    this.register(Mantle.getResource("set"), setLoader);
  }

  /** Creates a new set predicate given the passed values */
  public IJsonPredicate<T> setOf(Set<R> values) {
    return new SetPredicate(values);
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
}

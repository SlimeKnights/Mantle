package slimeknights.mantle.data.predicate;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;

/** Predicate registry that implements tag and set predicates */
public class RegistryPredicateRegistry<R,T> extends TagPredicateRegistry<R,T> {
  private final Function<T,R> getter;
  private final RecordLoadable<SetPredicate> setLoader;

  /**
   * Creates a new registry for predicates that mirrors an object registry.
   * @param name            Name to display in error messages
   * @param anyInstance     Any instance that always returns true. Will be used for nulls and missing fields.
   * @param noneInstance    None instance that always returns false. Used as the default for the conditional loader. If null, no none is registered.
   * @param registry        Loading logic for the backing registry
   * @param getter          Method mapping from the predicate type to the registry type
   * @param setKey          Key to use for the set predicate
   * @param tagKey          Loader for tag keys
   * @param tagMatcher      Logic to match a tag for the passed type
   */
  public RegistryPredicateRegistry(String name, IJsonPredicate<T> anyInstance, @Nullable IJsonPredicate<T> noneInstance, Loadable<R> registry, Function<T,R> getter, String setKey, Loadable<TagKey<R>> tagKey, BiPredicate<TagKey<R>,T> tagMatcher) {
    super(name, anyInstance, noneInstance, tagKey, tagMatcher);
    this.getter = getter;
    this.setLoader = RecordLoadable.create(registry.set().requiredField(setKey, p -> p.set), SetPredicate::new);
    this.register(Mantle.getResource("set"), setLoader);
  }

  /** @deprecated use {@link #RegistryPredicateRegistry(String, IJsonPredicate, IJsonPredicate, Loadable, Function, String, Loadable, BiPredicate)} */
  @Deprecated(forRemoval = true)
  public RegistryPredicateRegistry(String name, IJsonPredicate<T> anyInstance, Loadable<R> registry, Function<T,R> getter, String setKey, Loadable<TagKey<R>> tagKey, BiPredicate<TagKey<R>,T> tagMatcher) {
    this(name, anyInstance, null, registry, getter, setKey, tagKey, tagMatcher);
  }

  /** Creates a new set predicate given the passed values */
  public IJsonPredicate<T> setOf(Set<R> values) {
    return new SetPredicate(values);
  }

  /** Creates a new set predicate given the passed values */
  @SafeVarargs
  public final IJsonPredicate<T> setOf(R... values) {
    return setOf(ImmutableSet.copyOf(values));
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
    public RecordLoadable<? extends IJsonPredicate<T>> getLoader() {
      return setLoader;
    }
  }
}

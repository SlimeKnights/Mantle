package slimeknights.mantle.data.predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.mapping.ConditionalLoadable.ConditionalObject;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.registry.DefaultingLoaderRegistry;
import slimeknights.mantle.util.DataLoadedConditionContext;

import javax.annotation.Nullable;
import java.util.List;

/** Extension of generic loader registry providing default implementations for common predicates */
public class PredicateRegistry<T> extends DefaultingLoaderRegistry<IJsonPredicate<T>> {
  /** Loader for inverted predicates */
  private final RecordLoadable<InvertedJsonPredicate> invertedLoader;
  /** Loader for and predicates */
  private final RecordLoadable<AndJsonPredicate> andLoader;
  /** Loader for or predicates */
  private final RecordLoadable<OrJsonPredicate> orLoader;
  /** None instance for the conditional predicate */
  @Nullable
  private final IJsonPredicate<T> noneInstance;

  /**
   * Creates a new registry for predicates.
   * @param name          Name to display in error messages
   * @param anyInstance   Any instance that always returns true. Will be used for nulls and missing fields.
   * @param noneInstance  None instance that always returns false. Used as the default for the conditional loader. If null, no none is registered.
   */
  public PredicateRegistry(String name, IJsonPredicate<T> anyInstance, @Nullable IJsonPredicate<T> noneInstance) {
    super(name, anyInstance, noneInstance, true);
    this.noneInstance = noneInstance;
    // create common types
    Loadable<List<IJsonPredicate<T>>> list = this.list(2);
    invertedLoader = RecordLoadable.create(directField("inverted_type", p -> p.predicate), InvertedJsonPredicate::new);
    andLoader = RecordLoadable.create(list.requiredField("predicates", p -> p.children), AndJsonPredicate::new);
    orLoader = RecordLoadable.create(list.requiredField("predicates", p -> p.children), OrJsonPredicate::new);
    // register common types
    this.register(Mantle.getResource("any"), anyInstance.getLoader());
    if (noneInstance != null) {
      this.register(Mantle.getResource("none"), noneInstance.getLoader());
    }
    this.register(Mantle.getResource("inverted"), invertedLoader);
    this.register(Mantle.getResource("and"), andLoader);
    this.register(Mantle.getResource("or"), orLoader);
  }

  /** @deprecated use {@link #PredicateRegistry(String, IJsonPredicate, IJsonPredicate)} */
  @Deprecated(forRemoval = true)
  public PredicateRegistry(String name, IJsonPredicate<T> anyInstance) {
    this(name, anyInstance, null);
  }

  /**
   * Inverts the given predicate
   * @param predicate  Predicate to invert
   * @return  Inverted predicate
   */
  public IJsonPredicate<T> invert(IJsonPredicate<T> predicate) {
    return new InvertedJsonPredicate(predicate);
  }

  /**
   * Ands the given predicates together
   * @param predicates  Predicate list
   * @return  Predicate that is true if all the passed predicates are true
   */
  public IJsonPredicate<T> and(List<IJsonPredicate<T>> predicates) {
    return new AndJsonPredicate(predicates);
  }

  /**
   * Ors the given predicates together
   * @param predicates  Predicate list
   * @return  Predicate that is true if any of the passed predicates are true
   */
  public IJsonPredicate<T> or(List<IJsonPredicate<T>> predicates) {
    return new OrJsonPredicate(predicates);
  }

  /**
   * Predicate that based on a load condition will use change the predicate loaded.
   * @param ifTrue     Predicate to use if the condition is true.
   * @param ifFalse    Predicate to use if the condition is false.
   * @param conditions List of conditions that must match. to use {@code ifTrue}
   * @return  Predicate for datagen.
   */
  public IJsonPredicate<T> conditional(IJsonPredicate<T> ifTrue, IJsonPredicate<T> ifFalse, ICondition... conditions) {
    return new ConditionalPredicate(conditions, ifTrue, ifFalse);
  }

  /**
   * Predicate that based on a load condition will use change the predicate loaded.
   * @param ifTrue     Predicate to use if the condition is true.
   * @param conditions List of conditions that must match. to use {@code ifTrue}
   * @return  Predicate for datagen.
   */
  @SuppressWarnings("unused") // API
  public IJsonPredicate<T> conditional(IJsonPredicate<T> ifTrue, ICondition... conditions) {
    if (noneInstance == null) {
      throw new UnsupportedOperationException(getName() + " does not support unset ifFalse");
    }
    return conditional(ifTrue, noneInstance, conditions);
  }


  /** Predicate that inverts the condition. */
  @RequiredArgsConstructor
  public class InvertedJsonPredicate implements IJsonPredicate<T> {
    private final IJsonPredicate<T> predicate;

    @Override
    public boolean matches(T input) {
      return !predicate.matches(input);
    }

    @Override
    public RecordLoadable<? extends IJsonPredicate<T>> getLoader() {
      return invertedLoader;
    }

    @Override
    public IJsonPredicate<T> inverted() {
      return predicate;
    }
  }

  /** Predicate that requires all children to match */
  @RequiredArgsConstructor
  public class AndJsonPredicate implements IJsonPredicate<T> {
    private final List<IJsonPredicate<T>> children;

    @Override
    public boolean matches(T input) {
      for (IJsonPredicate<T> child : children) {
        if (!child.matches(input)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public IJsonPredicate<T> inverted() {
      return invert(this);
    }

    @Override
    public RecordLoadable<? extends IJsonPredicate<T>> getLoader() {
      return andLoader;
    }
  }

  /** Predicate that requires any child to match */
  @RequiredArgsConstructor
  public class OrJsonPredicate implements IJsonPredicate<T> {
    private final List<IJsonPredicate<T>> children;

    @Override
    public boolean matches(T input) {
      for (IJsonPredicate<T> child : children) {
        if (child.matches(input)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public IJsonPredicate<T> inverted() {
      return invert(this);
    }

    @Override
    public RecordLoadable<? extends IJsonPredicate<T>> getLoader() {
      return orLoader;
    }
  }

  /** Predicate that runs a load condition, should be used only in datagen. */
  @Accessors(fluent = true)
  @Getter
  @RequiredArgsConstructor
  public class ConditionalPredicate implements IJsonPredicate<T>, ConditionalObject<IJsonPredicate<T>> {
    private final ICondition[] conditions;
    private final IJsonPredicate<T> ifTrue;
    private final IJsonPredicate<T> ifFalse;

    @Override
    public boolean matches(T input) {
      // should be unused, but just in case
      for (ICondition condition : conditions) {
        if (!condition.test(DataLoadedConditionContext.INSTANCE)) {
          return ifFalse.matches(input);
        }
      }
      return ifTrue.matches(input);
    }

    @Override
    public IJsonPredicate<T> inverted() {
      return new ConditionalPredicate(conditions, ifFalse, ifTrue);
    }

    @Override
    public RecordLoadable<? extends IJsonPredicate<T>> getLoader() {
      return getConditionalLoader();
    }
  }
}

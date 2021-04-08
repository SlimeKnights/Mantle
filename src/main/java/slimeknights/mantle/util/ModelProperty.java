package slimeknights.mantle.util;

import java.util.function.Predicate;

public class ModelProperty<T> implements Predicate<T> {

  private final Predicate<T> pred;

  public ModelProperty() {
    this(t -> true);
  }

  public ModelProperty(Predicate<T> pred) {
    this.pred = pred;
  }

  @Override
  public boolean test(T t) {
    return pred.test(t);
  }
}
package slimeknights.mantle.util;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

/**
 * Implementation of {@link Consumer} that weakly references a parent object.
 * @param <TE>  Parent object type, typically a TE
 * @param <C>   Consumer value
 */
public class WeakConsumerWrapper<TE,C> implements Consumer<C> {
  private final WeakReference<TE> te;
  private final NonnullBiConsumer<TE,C> consumer;

  /**
   * Creates a new weak consumer wrapper
   * @param te        Weak reference, typically to a TE
   * @param consumer  Consumer using the TE and the consumed value. Should not use a lambda reference to an object that may need to be garbage collected
   */
  public WeakConsumerWrapper(TE te, NonnullBiConsumer<TE,C> consumer) {
    this.te = new WeakReference<>(te);
    this.consumer = consumer;
  }

  @Override
  public void accept(C c) {
    TE te = this.te.get();
    if (te != null) {
      consumer.accept(te, c);
    }
  }
}

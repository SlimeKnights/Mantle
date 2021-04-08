package slimeknights.mantle.util;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * Implementation of {@link NotNullConsumer} that weakly references a parent object.
 * to prevent the capability owner from keeping a reference to the listener TE and preventing garbage collection.
 * @param <TE>  Parent object type, typically a TE
 * @param <C>   Consumer value
 */
public class WeakConsumerWrapper<TE,C> implements NotNullConsumer<C> {
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
  public void accept(@NotNull C c) {
    TE te = this.te.get();
    if (te != null) {
      consumer.accept(te, c);
    }
  }
}

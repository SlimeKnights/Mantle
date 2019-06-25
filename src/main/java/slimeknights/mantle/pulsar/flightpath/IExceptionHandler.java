package slimeknights.mantle.pulsar.flightpath;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * General interface for an exception handler.
 *
 * @author Arkan <arkan@drakon.io>
 */
@ParametersAreNonnullByDefault
public interface IExceptionHandler {

  /**
   * Handle a given exception.
   *
   * @param ex The exception (duh)
   */
  void handle(Exception ex);

  /**
   * Called after all methods have been invoked for cleanup or coalescing.
   */
  void flush();

}

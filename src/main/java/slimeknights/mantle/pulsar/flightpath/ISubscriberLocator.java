package slimeknights.mantle.pulsar.flightpath;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Interface for defining custom subscriber scanners.
 */
@ParametersAreNonnullByDefault
public interface ISubscriberLocator {

    @Nonnull
    Map<Class, Set<Method>> findSubscribers(Object obj);

}

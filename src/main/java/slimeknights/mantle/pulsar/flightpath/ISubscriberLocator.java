package slimeknights.mantle.pulsar.flightpath;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface for defining custom subscriber scanners.
 */
@ParametersAreNonnullByDefault
public interface ISubscriberLocator {

    @Nonnull
    Map<Class<?>, Map<Method, Type>> findSubscribers(Object obj);

}

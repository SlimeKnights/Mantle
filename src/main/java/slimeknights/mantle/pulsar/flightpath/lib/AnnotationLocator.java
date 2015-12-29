package slimeknights.mantle.pulsar.flightpath.lib;

import slimeknights.mantle.pulsar.flightpath.ISubscriberLocator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Default implementation of ISubscriberLocator
 *
 * @author Arkan <arkan@drakon.io>
 */
@ParametersAreNonnullByDefault
public class AnnotationLocator implements ISubscriberLocator {

    private final Class<? extends Annotation> annotation;

    public AnnotationLocator(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public @Nonnull Map<Class, Set<Method>> findSubscribers(Object obj) {
        Map<Class, Set<Method>> methods = new HashMap<Class, Set<Method>>();
        for (Method m : obj.getClass().getMethods()) {
            if (m.isAnnotationPresent(annotation) && m.getParameterCount() == 1) {
                Class param = m.getParameterTypes()[0];
                if (!methods.containsKey(param)) methods.put(param, new HashSet<Method>());
                methods.get(param).add(m);
            }
        }
        return methods;
    }

}

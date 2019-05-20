package slimeknights.mantle.pulsar.flightpath.lib;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraftforge.eventbus.api.IGenericEvent;
import slimeknights.mantle.pulsar.flightpath.ISubscriberLocator;

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
    public @Nonnull Map<Class<?>, Map<Method, Type>> findSubscribers(Object obj) {
        Map<Class<?>, Map<Method, Type>> methods = new HashMap<Class<?>, Map<Method, Type>>();

        for (Method m : obj.getClass().getMethods()) {
            if (m.isAnnotationPresent(annotation) && m.getParameterTypes().length == 1) {
                Class<?> param = m.getParameterTypes()[0];

                Type filter = null;

                if(IGenericEvent.class.isAssignableFrom(param)) {
                    Type type = m.getGenericParameterTypes()[0];

                    if (type instanceof ParameterizedType) {
                        filter = ((ParameterizedType) type).getActualTypeArguments()[0];

                        if (filter instanceof ParameterizedType) {
                            filter = ((ParameterizedType) filter).getRawType();
                        }
                    }
                }

                if (!methods.containsKey(param)) methods.put(param, new HashMap<Method, Type>());
                methods.get(param).put(m, filter);
            }
        }

        return methods;
    }
}

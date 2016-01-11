package slimeknights.mantle.pulsar.flightpath;

import slimeknights.mantle.pulsar.flightpath.lib.AnnotationLocator;
import slimeknights.mantle.pulsar.flightpath.lib.BlackholeExceptionHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Flightpath - an ordered event bus implementation.
 *
 * @author Arkan <arkan@drakon.io>
 */
@ParametersAreNonnullByDefault
@SuppressWarnings({"unused","unchecked"})
public class Flightpath {

    private final ISubscriberLocator locator;
    // Note we *MUST* use a linked map, otherwise order is lost.
    private final LinkedHashMap<Object, Map<Class, Set<Method>>> subscribers = new LinkedHashMap<Object,
            Map<Class, Set<Method>>>();
    // Anything that manipulates state *MUST* acquire this lock. It prevents awkward issues when iterating.
    private final Object lock = new Object();

    private IExceptionHandler exceptionHandler = new BlackholeExceptionHandler();

    /**
     * Generates a plain old Flightpath, using the default @Airdrop annotation.
     */
    public Flightpath() {
        this.locator = new AnnotationLocator(Airdrop.class);
    }

    /**
     * Generates a Flightpath using a different locator implementation.
     *
     * Pass a custom constructed instance of AnnotationLocator to use alternative annotations.
     *
     * @param locator The locator to be applied to new objects.
     */
    public Flightpath(ISubscriberLocator locator) {
        this.locator = locator;
    }

    /**
     * Used to change exception handling behaviour.
     *
     * @param handler The handler to use.
     */
    public void setExceptionHandler(IExceptionHandler handler) {
        synchronized (lock) {
            this.exceptionHandler = handler;
        }
    }

    /**
     * Registers a given object onto the bus.
     *
     * @param obj Object to attach to the bus.
     */
    public void register(Object obj) {
        synchronized (lock) {
            if (subscribers.containsKey(obj)) return; // Nothing to do.
            subscribers.put(obj, locator.findSubscribers(obj));
        }
    }

    /**
     * Posts the given event on the bus.
     *
     * By default this blackholes exceptions. If you need different behaviour, see setExceptionHandler.
     *
     * @param evt The event to post.
     */
    public void post(Object evt) {
        synchronized (lock) {
            for (Map.Entry<Object, Map<Class, Set<Method>>> ent : subscribers.entrySet()) {
                for (Map.Entry<Class, Set<Method>> objEnt: ent.getValue().entrySet()) {
                    if (!objEnt.getKey().isAssignableFrom(evt.getClass())) continue;
                    Set<Method> ms = objEnt.getValue();
                    for (Method m : ms) {
                        try {
                            boolean access = m.isAccessible();
                            m.setAccessible(true);
                            m.invoke(ent.getKey(), evt);
                            m.setAccessible(access);
                        } catch (Exception ex) {
                            exceptionHandler.handle(ex);
                        }
                    }
                }
            }
            exceptionHandler.flush();
        }
    }

}

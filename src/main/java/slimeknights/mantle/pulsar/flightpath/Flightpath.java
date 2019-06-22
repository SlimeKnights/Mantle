package slimeknights.mantle.pulsar.flightpath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IGenericEvent;
import slimeknights.mantle.pulsar.flightpath.lib.AnnotationLocator;
import slimeknights.mantle.pulsar.flightpath.lib.BlackholeExceptionHandler;

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
    private final LinkedHashMap<Object, Map<Class<?>, Map<Method, Type>>> subscribers = new LinkedHashMap<Object, Map<Class<?>, Map<Method, Type>>>();
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
        synchronized (this.lock) {
            this.exceptionHandler = handler;
        }
    }

    /**
     * Registers a given object onto the bus.
     *
     * @param obj Object to attach to the bus.
     */
    public void register(Object obj) {
        synchronized (this.lock) {
            if (this.subscribers.containsKey(obj)) return; // Nothing to do.
            this.subscribers.put(obj, this.locator.findSubscribers(obj));
        }
    }

    /**
     * Posts the given event on the bus.
     *
     * By default this blackholes exceptions. If you need different behaviour, see setExceptionHandler.
     *
     * @param evt The event to post.
     */
    public void post(Event evt) {
        synchronized (this.lock) {
            for (Map.Entry<Object, Map<Class<?>, Map<Method, Type>>> ent : this.subscribers.entrySet()) {
                for (Map.Entry<Class<?>, Map<Method, Type>> obj: ent.getValue().entrySet()) {
                    if (!obj.getKey().isAssignableFrom(evt.getClass())) continue;
                    for(Map.Entry<Method, Type> objEnt : obj.getValue().entrySet()){
                        Method m = objEnt.getKey();
                        Type filter = objEnt.getValue();

                        try {
                            if (filter == null || filter == ((IGenericEvent)evt).getGenericType()) {
                                boolean access = m.isAccessible();
                                m.setAccessible(true);
                                m.invoke(ent.getKey(), evt);
                                m.setAccessible(access);
                            }
                        } catch(InvocationTargetException ex) {
                            // thrown when a method throws an exception, so use that exception instead of the wrapper for a better stacktrace
                            if(ex.getTargetException() instanceof Exception) {
                                this.exceptionHandler.handle((Exception)ex.getTargetException());
                            } else {
                                this.exceptionHandler.handle(ex);
                            }
                        } catch (Exception ex) {
                            // all other exceptions, this means something when wrong with the reflection
                            this.exceptionHandler.handle(ex);
                        }
                    }
                }
            }
            this.exceptionHandler.flush();
        }
    }

}

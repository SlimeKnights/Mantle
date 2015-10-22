package slimeknights.mantle.pulsar.internal;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Needed because Google EventBus is a derp and by default swallows exceptions (dafuq guys?)
 */
public final class BusExceptionHandler implements SubscriberExceptionHandler {
    private final String id, pulseId;

    /**
     * @param id Mod ID to include in exception raises.
     */
    public BusExceptionHandler(String id, String pulseId) {
        this.id = id;
        this.pulseId = pulseId;
    }

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext ctx) {
        FMLCommonHandler.instance().raiseException(exception, "Pulsar/" + id + "/" + pulseId
                + " >> Exception uncaught in [" + ctx.getSubscriber().getClass().getName() + ":"
                + ctx.getSubscriberMethod().getName() + "] for event [" + ctx.getEvent().getClass().getSimpleName()
                + "]", true);
    }
}

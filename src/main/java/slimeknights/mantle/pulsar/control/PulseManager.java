package slimeknights.mantle.pulsar.control;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import mantle.pulsar.config.IConfiguration;
import mantle.pulsar.internal.Configuration;
import mantle.pulsar.internal.CrashHandler;
import mantle.pulsar.internal.logging.ILogger;
import mantle.pulsar.internal.logging.LogManager;
import mantle.pulsar.pulse.Pulse;
import mantle.pulsar.pulse.PulseMeta;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * Manager class for a given mods Pulses.
 *
 * This MUST be constructed by a mod BEFORE preinit as it registers on to the mod event bus - a static block would serve
 * for this. No more Pulses can be registered after preinit has been caught, so assume preinit is too late to register
 * new Pulses.
 *
 * Each Pulsar-enabled mod should create one and only one of these to manage its Pulses.
 *
 * @author Arkan <arkan@drakon.io>
 */
@SuppressWarnings("unused")
public class PulseManager
{

    private ILogger log;

    private final boolean useConfig;

    private final LinkedHashMap<Object, PulseMeta> pulses = new LinkedHashMap<Object, PulseMeta>();

    private boolean blockNewRegistrations = false;

    private boolean configLoaded = false;

    private IConfiguration conf;

    private EventBus bus;

    private String id;

    /**
     * Configuration-using constructor.
     *
     * This form creates a PulseManager that supports configuration of Pulses by file.
     *
     * @param configName The config file name.
     */
    public PulseManager(String configName)
    {
        this.init();
        this.useConfig = true;
        this.conf = new Configuration(configName, this.log);
    }

    /**
     * Custom configuration-using constructor.
     *
     * Don't like JSON? Heathen. Lets you handle configuration, to whatever media you like - File, database, death star.
     * Whatever really. See {@link mantle.pulsar.config.IConfiguration}.
     *
     * @param config Configuration handler.
     */
    public PulseManager(IConfiguration config)
    {
        this.init();
        this.useConfig = true;
        this.conf = config;
    }

    /**
     * Shared initialiser code between all the constructors.
     */
    private void init()
    {
        String modId = Loader.instance().activeModContainer().getModId();
        this.id = modId;
        this.log = LogManager.getLogger("Pulsar-" + modId);
        FMLCommonHandler.instance().registerCrashCallable(new CrashHandler(modId, this));
        // Attach us to the mods FML bus and setup our own bus
        this.bus = new EventBus(new BusExceptionHandler(modId));
        this.attachToContainerEventBus(this);
    }

    /**
     * Register a new Pulse with the manager.
     *
     * This CANNOT be done after preInit() has been invoked.
     *
     * @param pulse The Pulse to register.
     */
    public void registerPulse(Object pulse)
    {
        if (this.blockNewRegistrations)
        {
            throw new RuntimeException("A mod tried to register a plugin after preinit! Pulse: "
                    + pulse);
        }
        if (!this.configLoaded)
        {
            this.conf.load();
            this.configLoaded = true;
        }

        String id, description, deps;
        boolean forced, enabled, defaultEnabled, missingDeps = false;

        try
        {
            Pulse p = pulse.getClass().getAnnotation(Pulse.class);
            id = p.id();
            description = p.description();
            deps = p.modsRequired();
            forced = p.forced();
            enabled = p.defaultEnable();
            defaultEnabled = p.defaultEnable();
        }
        catch (NullPointerException ex)
        {
            throw new RuntimeException("Could not parse @Pulse annotation for Pulse: " + pulse);
        }

        // Work around Java not allowing default-null fields.
        if (description.equals(""))
        {
            description = null;
        }

        if (!deps.equals(""))
        {
            String[] parsedDeps = deps.split(";");
            for (String s : parsedDeps)
            {
                if (!Loader.isModLoaded(s))
                {
                    this.log.info("Skipping Pulse " + id + "; missing dependency: " + s);
                    missingDeps = true;
                    enabled = false;
                    break;
                }
            }
        }

        PulseMeta meta = new PulseMeta(id, description, forced, enabled, defaultEnabled);
        meta.setEnabled(!missingDeps && this.getEnabledFromConfig(meta));

        if (meta.isEnabled())
        {
            this.pulses.put(pulse, meta);
            // Attach Pulse to internal event bus
            this.bus.register(pulse);
        }
    }

    /**
     * Helper to attach a given object to the modcontainer event bus.
     *
     * @param obj Object to register.
     */
    private void attachToContainerEventBus(Object obj)
    {
        ModContainer cnt = Loader.instance().activeModContainer();
        this.log.debug("Attaching [" + obj + "] to event bus for container [" + cnt + "]");
        try
        {
            FMLModContainer mc = (FMLModContainer) cnt;
            Field ebf = mc.getClass().getDeclaredField("eventBus");

            boolean access = ebf.isAccessible();
            ebf.setAccessible(true);
            EventBus eb = (EventBus) ebf.get(mc);
            ebf.setAccessible(access);

            eb.register(obj);
        }
        catch (NoSuchFieldException nsfe)
        {
            throw new RuntimeException("Pulsar >> Incompatible FML mod container (missing eventBus field) - wrong Forge version?");
        }
        catch (IllegalAccessException iae)
        {
            throw new RuntimeException("Pulsar >> Security Manager blocked access to eventBus on mod container. Cannot continue.");
        }
        catch (ClassCastException cce)
        {
            throw new RuntimeException("Pulsar >> Something in the mod container had the wrong type? " + cce.getMessage());
        }
    }

    /**
     * Internal (but public for EventBus use) handler for events.
     *
     * DO NOT CALL THIS DIRECTLY! Let EventBus handle it.
     *
     * @param evt An event object.
     */
    @Subscribe
    public void propagateEvent(FMLEvent evt)
    {
        if (evt instanceof FMLPreInitializationEvent)
        {
            this.preInit((FMLPreInitializationEvent) evt);
        }
        this.bus.post(evt);
    }

    private boolean getEnabledFromConfig(PulseMeta meta)
    {
        if (meta.isForced() || !this.useConfig)
        {
            return true; // Forced or no config set.
        }

        return this.conf.isModuleEnabled(meta);
    }

    public void preInit(FMLPreInitializationEvent evt)
    {
        if (!this.blockNewRegistrations)
        {
            this.conf.flush(); // First preInit call, so flush config
        }
        this.blockNewRegistrations = true;
    }

    /**
     * Check if a given Pulse ID is loaded in this manager.
     *
     * @param pulseId The ID to check.
     * @return Whether the ID was present.
     */
    public boolean isPulseLoaded(String pulseId)
    {
        for (Map.Entry<Object, PulseMeta> entry : this.pulses.entrySet())
        {
            if (entry.getValue().getId().equals(pulseId))
            {
                return true;
            }
        }
        return false;
    }

    public Collection<PulseMeta> getAllPulseMetadata()
    {
        return this.pulses.values();
    }

    /**
     * Needed because Google EventBus is a derp and by default swallows exceptions (dafuq guys?)
     */
    private class BusExceptionHandler implements SubscriberExceptionHandler
    {
        private final String id;

        /**
         * @param id Mod ID to include in exception raises.
         */
        public BusExceptionHandler(String id)
        {
            this.id = id;
        }

        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext ctx)
        {
            FMLCommonHandler.instance().raiseException(exception, "Pulsar/" + this.id + " >> Exception uncaught in ["
                    + ctx.getSubscriber().getClass().getName() + ":" + ctx.getSubscriberMethod().getName()
                    + "] for event [" + ctx.getEvent().getClass().getSimpleName() + "]", true);
        }
    }

    @Override
    public String toString()
    {
        return "PulseManager[" + this.id + "]";
    }
}

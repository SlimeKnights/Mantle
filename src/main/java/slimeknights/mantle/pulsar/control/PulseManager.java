package slimeknights.mantle.pulsar.control;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import slimeknights.mantle.pulsar.config.IConfiguration;
import slimeknights.mantle.pulsar.flightpath.Flightpath;
import slimeknights.mantle.pulsar.flightpath.IExceptionHandler;
import slimeknights.mantle.pulsar.flightpath.lib.AnnotationLocator;
import slimeknights.mantle.pulsar.internal.BusExceptionHandler;
import slimeknights.mantle.pulsar.internal.Configuration;
import slimeknights.mantle.pulsar.internal.CrashHandler;
import slimeknights.mantle.pulsar.pulse.Pulse;
import slimeknights.mantle.pulsar.pulse.PulseMeta;

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
@ParametersAreNonnullByDefault
public class PulseManager {

    private Logger log;
    private final boolean useConfig;

    private static final Map<String, Map<Object, PulseMeta>> ALL_PULSES = new HashMap<>();
    private final Map<Object, PulseMeta> pulses = new LinkedHashMap<>();
    private final Map<Object, PulseMeta> allPulses = new LinkedHashMap<>();
    // Use the Google @Subscribe to avoid confusion/breaking changes.
    private final Flightpath flightpath = new Flightpath(new AnnotationLocator(SubscribeEvent.class));

    private boolean blockNewRegistrations = false;
    private boolean configLoaded = false;
    private IConfiguration conf;
    private String id;

    public static final Marker CONFIG = MarkerManager.getMarker("PULSARCONFIG");

    /**
     * Configuration-using constructor.
     *
     * This form creates a PulseManager that supports configuration of Pulses by file.
     *
     * @param configName The config file name.
     */
    public PulseManager(String configName) {
        this.init();
        this.useConfig = true;
        this.conf = new Configuration(configName, this.log);
    }

    /**
     * Custom configuration-using constructor.
     *
     * Don't like JSON? Heathen. Lets you handle configuration, to whatever media you like - File, database, death star.
     * Whatever really. See {@link slimeknights.mantle.pulsar.config.IConfiguration}.
     *
     * @param config Configuration handler.
     */
    public PulseManager(IConfiguration config) {
        this.init();
        this.useConfig = true;
        this.conf = config;
    }

    /**
     * Shared initialiser code between all the constructors.
     */
    private void init() {
        String modId = ModLoadingContext.get().getActiveContainer().getNamespace();
        this.id = modId;
        this.log = LogManager.getLogger("Pulsar-" + modId);
        this.flightpath.setExceptionHandler(new BusExceptionHandler(modId));
        CrashReportExtender.registerCrashCallable(new CrashHandler(modId, this));
        // regsister our pulse loader so it can be found by the static method
        ALL_PULSES.put(modId, this.pulses);
        // Attach us to the mods FML bus
        this.attachToContainerEventBus(this);
    }

    /**
     * Overrides Pulsars default behaviour when a pulse emits an exception. See Flightpath's documentation.
     *
     * @param handler The Flightpath-compatible exception handler to use.
     */
    public void setPulseExceptionHandler(IExceptionHandler handler) {
        this.flightpath.setExceptionHandler(handler);
    }

    /**
     * Register a new Pulse with the manager.
     *
     * This CANNOT be done after preinit has been invoked.
     *
     * @param pulse The Pulse to register.
     */
    public void registerPulse(Object pulse) {
        if (this.blockNewRegistrations) throw new RuntimeException("A mod tried to register a plugin after preinit! Pulse: "
                + pulse);
        if (!this.configLoaded) {
            this.conf.load();

            this.configLoaded = true;
        }

        String id, description, deps, pulseDeps;
        boolean forced, enabled, defaultEnabled, missingDeps = false;

        try {
            Pulse p = pulse.getClass().getAnnotation(Pulse.class);
            id = p.id();
            description = p.description();
            deps = p.modsRequired();
            pulseDeps = p.pulsesRequired();
            forced = p.forced();
            enabled = p.defaultEnable();
            defaultEnabled = p.defaultEnable();
        } catch (NullPointerException ex) {
            throw new RuntimeException("Could not parse @Pulse annotation for Pulse: " + pulse);
        }

        // Work around Java not allowing default-null fields.
        if (description.equals("")) description = null;

        if (!deps.equals("")) {
            String[] parsedDeps = deps.split(";");
            for (String s : parsedDeps) {
                if (!ModList.get().isLoaded(s)) {
                    this.log.info("Skipping Pulse " + id + "; missing dependency: " + s);
                    missingDeps = true;
                    enabled = false;
                    break;
                }
            }
        }

        PulseMeta meta = new PulseMeta(id, description, forced, enabled, defaultEnabled);
        meta.setMissingDeps(missingDeps || !this.hasRequiredPulses(meta, pulseDeps));

        this.conf.addPulse(meta);
        this.allPulses.put(pulse, meta);
    }

    /**
     * Helper to attach a given object to the mod container event bus.
     *
     * @param obj Object to register.
     */
    private void attachToContainerEventBus(Object obj) {
        ModContainer cnt =  ModLoadingContext.get().getActiveContainer();
        this.log.debug("Attaching [" + obj + "] to event bus for container [" + cnt + "]");
        try {
            FMLModContainer mc = (FMLModContainer)cnt;
            Field ebf = mc.getClass().getDeclaredField("eventBus");

            boolean access = ebf.isAccessible();
            ebf.setAccessible(true);
            EventBus eb = (EventBus)ebf.get(mc);
            ebf.setAccessible(access);

            eb.register(obj);
        } catch (NoSuchFieldException nsfe) {
            throw new RuntimeException("Pulsar >> Incompatible FML mod container (missing eventBus field) - wrong Forge version?");
        } catch (IllegalAccessException iae) {
            throw new RuntimeException("Pulsar >> Security Manager blocked access to eventBus on mod container. Cannot continue.");
        } catch (ClassCastException cce) {
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
    @SubscribeEvent
    public void propagateEvent(Event evt) {
        if (evt instanceof FMLCommonSetupEvent)
            this.preInit((FMLCommonSetupEvent) evt);
        // We use individual buses due to the EventBus class using a Set rather than a List, thus losing the ordering.
        // This trick is shamelessly borrowed from FML.
        this.flightpath.post(evt);
    }

    private boolean getEnabledFromConfig(PulseMeta meta) {
        if (meta.isForced() || !this.useConfig) return true; // Forced or no config set.

        return this.conf.isModuleEnabled(meta);
    }

    private void preInit(FMLCommonSetupEvent evt) {
        if (!this.blockNewRegistrations)
            this.conf.flush(); // First init call, so flush config
        this.blockNewRegistrations = true;
    }

    private boolean hasRequiredPulses(PulseMeta meta, String deps) {
        if (!deps.equals("")) {
            String[] parsedDeps = deps.split(";");
            for (String s : parsedDeps) {
                if (!this.isPulseLoaded(s)) {
                    this.log.info("Skipping Pulse " + meta.getId() + "; missing pulse: " + s);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if a given Pulse ID is loaded in this manager.
     *
     * @param pulseId The ID to check. If it contains : treats the ID as a resource location and checks the global list
     * @return Whether the ID was present.
     */
    public boolean isPulseLoaded(String pulseId) {
        // if it has a colon, assume its a namespaced ID
        if(pulseId.contains(":")) {
            return isPulseLoadedGlobal(pulseId);
        }
        return isPulseLoaded(this.pulses, pulseId);
    }

    /**
     * Check if a given Pulse ID is loaded in any manager.
     *
     * @param pulseId Resource location string to check. Domain is the mod ID, path is the pulse ID
     * @return Whether the ID was present.
     */
    public static boolean isPulseLoadedGlobal(String pulseId) {
        ResourceLocation loc = new ResourceLocation(pulseId);
        Map<Object, PulseMeta> pulses = ALL_PULSES.get(loc.getNamespace());
        if(pulses != null) {
            return isPulseLoaded(pulses, loc.getPath());
        }
        return false;
    }

    /**
     * Checks whether a pulse is loaded in the given pulse manager
     *
     * @param pulseId The ID to check.
     * @return Whether the ID was present.
     */
    private static boolean isPulseLoaded(Map<Object, PulseMeta> pulses, String pulseId) {
        for(Map.Entry<Object, PulseMeta> entry : pulses.entrySet()) {
            if (entry.getValue().getId().equalsIgnoreCase(pulseId)) {
                return true;
            }
        }
        return false;
    }

    public Collection<PulseMeta> getAllPulseMetadata() {
        return this.pulses.values();
    }

    @Override
    public String toString() {
        return "PulseManager[" + this.id + "]";
    }
    /**
     * Enable or Disable all Pulses registered
     *
     * This MUST be called after you register all your pulses using registerPulse and must be called BEFORE preinit
     */
    public void enablePulses() {
        if(this.blockNewRegistrations) throw new RuntimeException("A mod tried to enable their plugins after preinit!");

        if(this.configLoaded)
            this.conf.postLoad();

        for (Map.Entry<Object, PulseMeta> entry : this.allPulses.entrySet()) {
            PulseMeta meta = entry.getValue();
            Object pulse = entry.getKey();

            meta.setEnabled(this.getEnabledFromConfig(meta));

            if (meta.isEnabled()) {
                this.pulses.put(pulse, meta);
                this.flightpath.register(pulse);
            }
        }
    }
}

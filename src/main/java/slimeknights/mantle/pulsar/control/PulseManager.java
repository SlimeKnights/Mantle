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
    private final boolean usingATomlConfig;

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
        init();
        useConfig = true;
        usingATomlConfig = false;
        conf = new Configuration(configName, log);
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
        init();
        useConfig = true;
        usingATomlConfig = config.isUsingTomlConfig();
        conf = config;
    }

    /**
     * Shared initialiser code between all the constructors.
     */
    private void init() {
        String modId = ModLoadingContext.get().getActiveContainer().getNamespace();
        this.id = modId;
        log = LogManager.getLogger("Pulsar-" + modId);
        flightpath.setExceptionHandler(new BusExceptionHandler(modId));
        CrashReportExtender.registerCrashCallable(new CrashHandler(modId, this));
        // regsister our pulse loader so it can be found by the static method
        ALL_PULSES.put(modId, pulses);
        // Attach us to the mods FML bus
        attachToContainerEventBus(this);
    }

    /**
     * Overrides Pulsars default behaviour when a pulse emits an exception. See Flightpath's documentation.
     *
     * @param handler The Flightpath-compatible exception handler to use.
     */
    public void setPulseExceptionHandler(IExceptionHandler handler) {
        flightpath.setExceptionHandler(handler);
    }

    /**
     * Register a new Pulse with the manager.
     *
     * This CANNOT be done after preinit has been invoked.
     *
     * @param pulse The Pulse to register.
     */
    public void registerPulse(Object pulse) {
        if (blockNewRegistrations) throw new RuntimeException("A mod tried to register a plugin after preinit! Pulse: "
                + pulse);
        if (!configLoaded) {
            if(!usingATomlConfig) conf.load();

            configLoaded = true;
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
                    log.info("Skipping Pulse " + id + "; missing dependency: " + s);
                    missingDeps = true;
                    enabled = false;
                    break;
                }
            }
        }

        PulseMeta meta = new PulseMeta(id, description, forced, enabled, defaultEnabled);
        meta.setMissingDeps(missingDeps || !hasRequiredPulses(meta, pulseDeps));

        if(usingATomlConfig) {
            conf.pushBuilder();
            meta.setConfigEntry(getConfigEntryForConfig(meta));
            allPulses.put(pulse, meta);
        } else {
            meta.setEnabled(getEnabledFromConfig(meta));

            if (meta.isEnabled()) {
                pulses.put(pulse, meta);
                flightpath.register(pulse);
            }
        }
    }

    /**
     * Helper to attach a given object to the mod container event bus.
     *
     * @param obj Object to register.
     */
    private void attachToContainerEventBus(Object obj) {
        ModContainer cnt =  ModLoadingContext.get().getActiveContainer();
        log.debug("Attaching [" + obj + "] to event bus for container [" + cnt + "]");
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
        if (evt instanceof FMLCommonSetupEvent) preInit((FMLCommonSetupEvent) evt);
        // We use individual buses due to the EventBus class using a Set rather than a List, thus losing the ordering.
        // This trick is shamelessly borrowed from FML.
        flightpath.post(evt);
    }

    private boolean getEnabledFromConfig(PulseMeta meta) {
        if (meta.isForced() || !useConfig) return true; // Forced or no config set.

        return conf.isModuleEnabled(meta);
    }

    private void preInit(FMLCommonSetupEvent evt) {
        if (!blockNewRegistrations) conf.flush(); // First init call, so flush config
        blockNewRegistrations = true;
    }

    private boolean hasRequiredPulses(PulseMeta meta, String deps) {
        if (!deps.equals("")) {
            String[] parsedDeps = deps.split(";");
            for (String s : parsedDeps) {
                if (!isPulseLoaded(s)) {
                    log.info("Skipping Pulse " + meta.getId() + "; missing pulse: " + s);
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
        return isPulseLoaded(pulses, pulseId);
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
            if (entry.getValue().getId().equals(pulseId)) {
                return true;
            }
        }
        return false;
    }

    public Collection<PulseMeta> getAllPulseMetadata() {
        return pulses.values();
    }

    @Override
    public String toString() {
        return "PulseManager[" + id + "]";
    }

    //TOML Support Start
    public void enablePulses() {
        if(usingATomlConfig) {
            conf.popBuilder();
            conf.load();

            for (Map.Entry<Object, PulseMeta> entry : allPulses.entrySet()) {
                PulseMeta meta = entry.getValue();
                Object pulse = entry.getKey();

                meta.setEnabled(getEnabledFromConfig(meta.getConfigEntry(), meta));

                if (meta.isEnabled()) {
                    pulses.put(pulse, meta);
                    flightpath.register(pulse);
                }
            }
        } else {
            log.info("Calling enablePulses isn't needed unless you are using a TOML file.");
        }
    }

    private BooleanValue getConfigEntryForConfig(PulseMeta meta) {
        if (meta.isForced() || !useConfig) return null;

        return conf.getConfigEntry(meta);
    }

    private boolean getEnabledFromConfig(BooleanValue boolValue, PulseMeta meta) {
        if (meta.isForced() || !useConfig) return true; // Forced or no config set.

        return conf.isModuleEnabled(boolValue);
    }
    //TOML Support End
}

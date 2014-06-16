package mantle.pulsar.control;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import mantle.pulsar.internal.PulseMeta;
import mantle.pulsar.pulse.IPulse;
import mantle.pulsar.pulse.Pulse;
import mantle.pulsar.pulse.PulseProxy;

/**
 * Manager class for a given mods Pulses.
 *
 * Each Pulsar-enabled mod should create one and only one of these to manage its Pulses.
 *
 * @author Arkan <arkan@drakon.io>
 */
public class PulseManager {

    private final Logger log;
    private final boolean useConfig;
    private final String configPath;

    private final HashMap<IPulse, PulseMeta> pulses = new HashMap<IPulse, PulseMeta>();

    private boolean blockNewRegistrations = false;

    /**
     * Configuration-less constructor.
     *
     * PulseManagers created this way ignore configurability on child Pulses. This is not recommended.
     *
     * @param modId The parents ModID.
     */
    public PulseManager(String modId) {
        log = LogManager.getLogger("PulseManager-" + modId);
        useConfig = false;
        configPath = null;
    }

    /**
     * Configuration-using constructor.
     *
     * This form creates a PulseManager that supports configuration of Pulses by file. Recommended approach.
     *
     * @param modId The parents ModID.
     * @param configName The config file name.
     */
    public PulseManager(String modId, String configName) {
        log = LogManager.getLogger("PulseManager-" + modId);
        useConfig = true;
        this.configPath = Loader.instance().getConfigDir().toString() + File.pathSeparator + configName;
    }

    /**
     * Register a new Pulse with the manager.
     *
     * This CANNOT be done after preInit() has been invoked.
     *
     * @param pulse The Pulse to register.
     */
    public void registerPulse(IPulse pulse) {
        if (blockNewRegistrations) throw new RuntimeException("A mod tried to register a plugin after preinit! Pulse: "
                + pulse);

        String id;
        boolean forced, enabled;

        try {
            Pulse p = pulse.getClass().getAnnotation(Pulse.class);
            id = p.id();
            forced = p.forced();
            enabled = p.defaultEnable();
        } catch (NullPointerException ex) {
            throw new RuntimeException("Could not parse @Pulse annotation for Pulse: " + pulse);
        }

        PulseMeta meta = new PulseMeta(id, forced, enabled);
        meta.setEnabled(getEnabledFromConfig(meta));

        if (meta.isEnabled()) {
            parseAndAddProxies(pulse);
            pulses.put(pulse, meta);
        }
    }

    private boolean getEnabledFromConfig(PulseMeta meta) {
        if (meta.isForced()) return true;
        // TODO: Check configuration and set enabled flag as needed.
        return true;
    }

    private void parseAndAddProxies(IPulse pulse) {
        try {
            for (Field f : pulse.getClass().getDeclaredFields()) {
                log.debug("Parsing field: " + f);
                PulseProxy p = f.getAnnotation(PulseProxy.class);
                if (p != null) {
                    boolean accessible = f.isAccessible();
                    f.setAccessible(true);
                    switch (FMLCommonHandler.instance().getSide()) {
                        case CLIENT:
                            f.set(pulse, Class.forName(p.client()).newInstance());
                            break;
                        default:
                            f.set(pulse, Class.forName(p.server()).newInstance());
                    }
                    f.setAccessible(accessible);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Pulse annotation parsing failed for Pulse " + pulse + "; " + ex);
        }
    }

    public void preInit(FMLPreInitializationEvent evt) {
        blockNewRegistrations = true;
        for (Map.Entry<IPulse, PulseMeta> e : pulses.entrySet()) {
            log.debug("Preinitialising Pulse " + e.getValue().getId() + "...");
            e.getKey().preInit(evt);
        }
    }

    public void init(FMLInitializationEvent evt) {
        for (Map.Entry<IPulse, PulseMeta> e : pulses.entrySet()) {
            log.debug("Initialising Pulse " + e.getValue().getId() + "...");
            e.getKey().init(evt);
        }
    }

    public void postInit(FMLPostInitializationEvent evt) {
        for (Map.Entry<IPulse, PulseMeta> e : pulses.entrySet()) {
            log.debug("Postinitialising Pulse " + e.getValue().getId() + "...");
            e.getKey().postInit(evt);
        }
    }

}

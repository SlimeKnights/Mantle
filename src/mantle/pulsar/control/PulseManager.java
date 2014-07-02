package mantle.pulsar.control;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import mantle.pulsar.internal.Configuration;
import mantle.pulsar.internal.PulseMeta;
import mantle.pulsar.internal.logging.ILogger;
import mantle.pulsar.internal.logging.LogManager;
import mantle.pulsar.pulse.Handler;
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
@SuppressWarnings({"unused", "deprecated"})
public class PulseManager {

    private final ILogger log;
    private final boolean useConfig;
    private final String configName;

    private final HashMap<Object, PulseMeta> pulses = new HashMap<Object, PulseMeta>();

    private boolean blockNewRegistrations = false;
    private Configuration conf = null;

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
        configName = null;
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
        this.configName = configName;
    }

    /**
     * Register a new Pulse with the manager.
     *
     * This CANNOT be done after preInit() has been invoked.
     *
     * @param pulse The Pulse to register.
     */
    public void registerPulse(Object pulse) {
        if (blockNewRegistrations) throw new RuntimeException("A mod tried to register a plugin after preinit! Pulse: "
                + pulse);

        String id, description;
        boolean forced, enabled;

        try {
            Pulse p = pulse.getClass().getAnnotation(Pulse.class);
            id = p.id();
            description = p.description();
            forced = p.forced();
            enabled = p.defaultEnable();
        } catch (NullPointerException ex) {
            throw new RuntimeException("Could not parse @Pulse annotation for Pulse: " + pulse);
        }

        if (description.equals("")) description = null; // Work around Java not allowing default-null fields.

        PulseMeta meta = new PulseMeta(id, description, forced, enabled);
        meta.setEnabled(getEnabledFromConfig(meta));

        if (meta.isEnabled()) {
            parseAndAddProxies(pulse);
            pulses.put(pulse, meta);
        }
    }

    private boolean getEnabledFromConfig(PulseMeta meta) {
        if (meta.isForced() || !useConfig) return true; // Forced or no config set.

        if (conf == null) {
            conf = new Configuration(configName, log);
        }

        return conf.isModuleEnabled(meta);
    }

    /**
     * @deprecated FML handles proxies now.
     *
     * @param pulse Pulse to parse for proxy annotations.
     */
    @Deprecated
    private void parseAndAddProxies(Object pulse) {
        try {
            for (Field f : pulse.getClass().getDeclaredFields()) {
                log.debug("Parsing field: " + f);
                PulseProxy p = f.getAnnotation(PulseProxy.class);
                if (p != null) { // Support for deprecated PulseProxy annotation
                    log.warn("Pulse " + pulse + " used the deprecated PulseProxy annotation. As of Pulsar 0.1.0, it's now preferred to use FML's SidedProxy annotation.");
                    log.warn("The old PulseProxy parsing will be removed in the next breaking update (Pulsar 1.x).");
                    setProxyField(pulse, f, p.clientSide(), p.serverSide());
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Pulse annotation parsing failed for Pulse " + pulse + "; " + ex);
        }
    }

    @Deprecated
    private void setProxyField(Object pulse, Field f, String client, String server) throws Exception {
        boolean accessible = f.isAccessible();
        f.setAccessible(true);
        switch (FMLCommonHandler.instance().getSide()) {
            case CLIENT:
                f.set(pulse, Class.forName(client).newInstance());
                break;
            default:
                f.set(pulse, Class.forName(server).newInstance());
        }
        f.setAccessible(accessible);
    }

    public void preInit(FMLPreInitializationEvent evt) {
        blockNewRegistrations = true;
        for (Map.Entry<Object, PulseMeta> e : pulses.entrySet()) {
            log.debug("Preinitialising Pulse " + e.getValue().getId() + "...");
            if (e.getKey() instanceof IPulse) { // Deprecated IPulse handling
                IPulse ip = (IPulse)e.getKey();
                ip.preInit(evt);
            } else findAndInvokeHandlers(e.getKey(), evt);
        }
    }

    public void init(FMLInitializationEvent evt) {
        for (Map.Entry<Object, PulseMeta> e : pulses.entrySet()) {
            log.debug("Initialising Pulse " + e.getValue().getId() + "...");

            if (e.getKey() instanceof IPulse) { // Deprecated IPulse handling
                IPulse ip = (IPulse)e.getKey();
                ip.init(evt);
                log.warn("Pulse " + e.getValue().getId() + " is using the deprecated IPulse interface.");
                log.warn("This will be removed in the next major version (Pulsar 1.x) - Please switch to @Handler!");
            } else findAndInvokeHandlers(e.getKey(), evt);
        }
    }

    public void postInit(FMLPostInitializationEvent evt) {
        for (Map.Entry<Object, PulseMeta> e : pulses.entrySet()) {
            log.debug("Postinitialising Pulse " + e.getValue().getId() + "...");

            if (e.getKey() instanceof IPulse) { // Deprecated IPulse handling
                IPulse ip = (IPulse)e.getKey();
                ip.postInit(evt);
            } else findAndInvokeHandlers(e.getKey(), evt);
        }
    }

    /**
     * Parse an object for a matching handler for the given object.
     *
     * @param pulse Object to inspect for Handlers
     * @param evt The event object
     */
    @SuppressWarnings("unchecked")
    private void findAndInvokeHandlers(Object pulse, Object evt) {
        for (Method m : pulse.getClass().getDeclaredMethods()) {
            try {
                if (m.getAnnotation(Handler.class) == null) continue; // Ignore non-@Handler methods

                Class[] pTypes = m.getParameterTypes();
                if (pTypes.length != 1) continue;

                Class pt = pTypes[0];
                if (pt.isAssignableFrom(evt.getClass())) {
                    m.invoke(pulse, evt);
                }
            } catch (Exception ex) {
                log.warn("Caught exception in findAndInvokeHandlers: " + ex);
                ex.printStackTrace();
            }
        }
    }

}

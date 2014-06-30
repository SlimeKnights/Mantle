package mantle.pulsar.pulse;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Base interface for all Pulsar modules ("pulses").
 *
 * @author Arkan <arkan@drakon.io>
 */
public interface IPulse {

    /**
     * FML preinit from the parent mod.
     *
     * @param event The FML event inherited from this Pulses parent.
     */
    public void preInit(FMLPreInitializationEvent event);

    /**
     * FML init from the parent mod.
     *
     * @param event The FML event inherited from this Pulses parent.
     */
    public void init(FMLInitializationEvent event);

    /**
     * FML postinit from the parent mod.
     *
     * @param event The FML event inherited from this Pulses parent.
     */
    public void postInit(FMLPostInitializationEvent event);

}

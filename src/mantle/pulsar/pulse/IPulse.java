package mantle.pulsar.pulse;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Base interface for all Pulsar modules ("pulses").
 *
 * @deprecated Use the new {@link Handler} annotation.
 *
 * @author Arkan <arkan@drakon.io>
 */
@Deprecated
public interface IPulse {

    /**
     * FML preinit from the parent mod.
     *
     * @param evt The FML event inherited from this Pulses parent.
     */
    public void preInit(FMLPreInitializationEvent evt);

    /**
     * FML init from the parent mod.
     *
     * @param evt The FML event inherited from this Pulses parent.
     */
    public void init(FMLInitializationEvent evt);

    /**
     * FML postinit from the parent mod.
     *
     * @param evt The FML event inherited from this Pulses parent.
     */
    public void postInit(FMLPostInitializationEvent evt);

}

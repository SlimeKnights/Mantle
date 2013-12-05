package mantle.internal.crash;

import cpw.mods.fml.common.ICrashCallable;

/**
 * Crash Callable for supported environments.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
public class CallableSuppConfig implements ICrashCallable
{

    @Override
    public String call () throws Exception
    {
        return "Environment healthy.";
    }

    @Override
    public String getLabel ()
    {
        return "Mantle Env";
    }

}

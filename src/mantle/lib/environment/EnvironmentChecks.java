package mantle.lib.environment;

import java.util.ArrayList;
import java.util.List;

import static mantle.lib.CoreRepo.*;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ICrashCallable;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import mantle.crash.*;
import mantle.lib.CoreConfig;

/**
 * Environment Checks
 *
 * Checks the runtime environment is safe for use. If not, registers warnings and adds a suitable crash callable.
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
public class EnvironmentChecks
{

    private EnvironmentChecks()
    {
    } // Singleton

    // Usable by other mods to detect Optifine.
    public static boolean hasOptifine = false;

    /**
     * Checks for conflicting stuff in environment; adds callable to any crash logs if so.
     * Note: This code adds additional data to crashlogs. It does not trigger any crashes.
     */
    public static void verifyEnvironmentSanity ()
    {
        List<String> modIds = new ArrayList<String>();

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT && FMLClientHandler.instance().hasOptifine() || Loader.isModLoaded("optifine"))
        {
            if (!CoreConfig.silenceEnvChecks) logger.error("[Environment Checks] Optifine detected. This may cause issues due to base edits or ASM usage.");
            hasOptifine = true;
            modIds.add("optifine");
        }

        try
        {
            Class cl = Class.forName("org.bukkit.Bukkit");
            if (cl != null)
            {
                if (!CoreConfig.silenceEnvChecks) logger.error("[Environment Checks] Bukkit implementation detected. This may cause issues. Bukkit implementations include Craftbukkit and MCPC+.");
                modIds.add("bukkit");
            }
        }
        catch (Exception ex)
        {
            // No Bukkit in environment.
        }

        if (modIds.size() == 0)
        {
            ICrashCallable callable = new CallableSuppConfig(modId);
            FMLCommonHandler.instance().registerCrashCallable(callable);
        }
        else
        {
            ICrashCallable callable = new CallableUnsuppConfig(modId, modIds);
            FMLCommonHandler.instance().registerCrashCallable(callable);
        }
    }

}

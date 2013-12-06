package mantle.internal;

import java.util.ArrayList;
import java.util.List;

import mantle.Mantle;
import mantle.lib.CoreRepo.*;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ICrashCallable;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import mantle.internal.crash.CallableUnsuppConfig;
import mantle.internal.crash.CallableSuppConfig;

/**
 * Environment Checks
 *
 * Checks the runtime environment is safe for use. If not, registers warnings and adds a suitable crash callable.
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
public class EnvironmentChecks
{

    public static boolean hasOptifine = false;

    private EnvironmentChecks()
    {
    } // Singleton

    /**
     * Checks for conflicting stuff in environment; adds callable to any crash logs if so.
     * Note: This code adds additional data to crashlogs. It does not trigger any crashes.
     */
    public static void verifyEnvironmentSanity ()
    {
        List<String> modIds = new ArrayList<String>();

        if (Loader.isModLoaded("gregtech_addon"))
        {
            Mantle.logger
                    .severe("[Environment Checks] Gelatinous iceberg dead ahead! Entering Greggy waters! Abandon hope all ye who enter here! (No, seriously, we don't support GT. Don't report any issues. Thanks.)");
            modIds.add("gregtech_addon");
        }

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT && FMLClientHandler.instance().hasOptifine() || Loader.isModLoaded("optifine"))
        {
            Mantle.logger.severe("[Environment Checks] Optifine detected. This is a Bad Thing(tm) and can crash Minecraft due to an Optifine bug during TCon armor renders! Capes also disabled.");
            modIds.add("optifine");
            hasOptifine = true;
        }

        try
        {
            Class cl = Class.forName("org.bukkit.Bukkit");
            if (cl != null)
            {
                Mantle.logger.severe("[Environment Checks] Bukkit implementation detected. This may be crashy. Bukkit implementations include Craftbukkit and MCPC+.");
                modIds.add("bukkit");
            }
        }
        catch (Exception ex)
        {
            // No Bukkit in environment.
        }

        try
        {
            Class cl = Class.forName("magic.launcher.Launcher");
            if (cl != null)
            {
                Mantle.logger.severe("[Environment Checks] Magic Launcher detected. We recommend using anything else. Vanilla's launcher works fine, as do others.");
                modIds.add("magic_launcher");
            }
        }
        catch (Exception ex)
        {
            // No derpy Magic Launcher in environment.
        }

        if (modIds.size() == 0)
        {
            ICrashCallable callable = new CallableSuppConfig();
            FMLCommonHandler.instance().registerCrashCallable(callable);
        }
        else
        {
            ICrashCallable callable = new CallableUnsuppConfig(modIds);
            FMLCommonHandler.instance().registerCrashCallable(callable);
        }
    }

}

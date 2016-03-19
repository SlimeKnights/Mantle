package slimeknights.mantle;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import slimeknights.mantle.common.CommonProxy;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(modid = Mantle.modId, name = Mantle.modName, version = Mantle.modVersion, dependencies = "required-after:Forge@[12.16,)", acceptedMinecraftVersions = "[1.9,]")
public class Mantle {

  public static final String modId = "mantle";
  public static final String modName = "Mantle";
  public static final String modVersion = "${version}";
  public static final Logger logger = LogManager.getLogger("Mantle");

  /* Instance of this mod, used for grabbing prototype fields */
  @Instance("mantle")
  public static Mantle instance;

  /* Proxies for sides, used for graphics processing */
  @SidedProxy(clientSide = "slimeknights.mantle.client.ClientProxy", serverSide = "slimeknights.mantle.common.CommonProxy")
  public static CommonProxy proxy;
}

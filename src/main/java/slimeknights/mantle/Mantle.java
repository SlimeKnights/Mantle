package slimeknights.mantle;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import slimeknights.mantle.client.ExtraHeartRenderHandler;
import slimeknights.mantle.common.CommonProxy;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(modid = Mantle.modId,
    name = Mantle.modName,
    version = Mantle.modVersion,
    dependencies = "required-after:forge@[13.19.0.2153,)",
    acceptedMinecraftVersions = "[1.11,1.12)")
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

  @Mod.EventHandler
  public void preInit(FMLPreInitializationEvent event){
    proxy.preInit();
  }

  @Mod.EventHandler
  public void postInit(FMLPostInitializationEvent event) {
    if(event.getSide().isClient()) {
      MinecraftForge.EVENT_BUS.register(new ExtraHeartRenderHandler());
    }
  }
}

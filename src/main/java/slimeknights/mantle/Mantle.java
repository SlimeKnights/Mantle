package slimeknights.mantle;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.client.ClientProxy;
import slimeknights.mantle.common.ServerProxy;

/**
 * Mantle
 *
 * Central mod object for Mantle
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(Mantle.modId)
public class Mantle {

  public static final String modId = "mantle";
  public static final Logger logger = LogManager.getLogger("Mantle");

  /* Instance of this mod, used for grabbing prototype fields */
  public static Mantle instance;

  /* Proxies for sides, used for graphics processing */
  public static ServerProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

  public Mantle() {
    instance = this;
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postInit);
  }

  private void preInit(final FMLCommonSetupEvent event) {
    proxy.preInit();
  }

  private void init(final InterModEnqueueEvent event) {
    proxy.init();
  }

  private void postInit(final InterModProcessEvent event) {
    proxy.postInit();
  }
}

package slimeknights.mantle.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;

import slimeknights.mantle.network.NetworkWrapper;

class ExampleSync {

  static NetworkWrapper networkWrapper;
  static ExampleSync INSTANCE;

  static void setup() {
    networkWrapper = new NetworkWrapper("mantle:example");
    networkWrapper.registerPacketClient(ExampleSyncPacketImpl.class);
    INSTANCE = new ExampleSync();
  }

  @SideOnly(Side.CLIENT)
  private static boolean needsRestart;

  @SubscribeEvent
  @SideOnly(Side.SERVER)
  public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if(event.player instanceof EntityPlayerMP && FMLCommonHandler.instance().getSide().isServer()) {
      ExampleSyncPacketImpl packet = new ExampleSyncPacketImpl();
      networkWrapper.network.sendTo(packet, (EntityPlayerMP) event.player);
    }
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void playerJoinedWorld(TickEvent.ClientTickEvent event) {
    EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
    if(needsRestart) {
      player.addChatMessage(new TextComponentString("Configs synced with server. Configs require a restart"));
    }
    else {
      player.addChatMessage(new TextComponentString("Configs synced with server."));
    }
    MinecraftForge.EVENT_BUS.unregister(this);
  }

  static class ExampleConfig extends AbstractConfig {
    static ExampleConfig INSTANCE = new ExampleConfig();

    ExampleConfigFile exampleConfigFile;

    // call from preinit or something
    public void onPreInit(FMLPreInitializationEvent event) {
      exampleConfigFile = this.load(new ExampleConfigFile(event.getModConfigurationDirectory()), ExampleConfigFile.class);

      // register this serverside to sync
      if(event.getSide().isServer()) {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
      }
    }
  }

  static class ExampleConfigFile extends AbstractConfigFile {

    public ExampleConfigFile(File configFolder) {
      super(configFolder, "exampleconfigfile");
    }

    @Override
    public void insertDefaults() {
      // no default values that need to initialized dynamically
    }
  }

  static class ExampleSyncPacketImpl extends AbstractConfigSyncPacket {

    @Override
    protected AbstractConfig getConfig() {
      return ExampleConfig.INSTANCE;
    }

    @Override
    protected boolean sync() {
      if(super.sync()) {
        // clientside register only
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        return true;
      }
      return false;
    }
  }
}

package slimeknights.mantle.multiblock;

/**
 * @deprecated  Slated for removal in 1.17. If you used this, talk to one of the devs and we can pull the updated verson from Tinkers Construct back
 */
@Deprecated
public interface IMasterNode extends IMasterLogic, IServantLogic {

  boolean isCurrentlyMaster();

  boolean isEquivalentMaster(IMasterLogic master);
}

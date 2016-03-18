package slimeknights.mantle.multiblock;

public interface IMasterNode extends IMasterLogic, IServantLogic
{
    boolean isCurrentlyMaster();

    boolean isEquivalentMaster(IMasterLogic master);
}

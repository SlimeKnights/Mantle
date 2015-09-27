package slimeknights.mantle.block;

public interface IMasterNode extends IMasterLogic, IServantLogic
{
    public boolean isCurrentlyMaster();

    public boolean isEquivalentMaster(IMasterLogic master);
}

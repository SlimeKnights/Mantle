package mantle.blocks.iface;

/**
 * Marks blocks that can be active and inactive.
 *
 * @author mDiyo
 * @author Sunstrike
 */
public interface IActiveLogic
{
    public boolean getActive ();

    public void setActive (boolean flag);
}

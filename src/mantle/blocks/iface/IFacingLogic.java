package mantle.blocks.iface;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Logic for blocks which have a specific direction facing.
 *
 * @author mDiyo
 */
public interface IFacingLogic
{

    public byte getRenderDirection ();

    public ForgeDirection getForgeDirection ();

    @Deprecated
    public void setDirection (int side);

    @Deprecated
    public void setDirection (float yaw, float pitch, EntityLivingBase player);

    //TODO 1.8 \/
    /** This will be added next version
    * public void setDirection(int side, float yaw, float pitch, EntityLivingBase player); */

}

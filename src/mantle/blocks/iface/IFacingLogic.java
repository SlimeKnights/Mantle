package mantle.blocks.iface;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.entity.EntityLivingBase;

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

    /** This will be added next version
    * public void setDirection(int side, float yaw, float pitch, EntityLivingBase player); */

}

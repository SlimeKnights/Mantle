package mantle.blocks.iface;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;

/**
 * Logic for blocks which have a specific direction facing.
 *
 * @author mDiyo
 */
public interface IFacingLogic
{

    public byte getRenderDirection();

    public EnumFacing getForgeDirection();

    public void setDirection(EnumFacing facing, float yaw, float pitch, EntityLivingBase player);

}

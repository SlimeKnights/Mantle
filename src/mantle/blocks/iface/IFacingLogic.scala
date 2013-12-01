package mantle.blocks.iface

import net.minecraftforge.common.ForgeDirection
import net.minecraft.entity.EntityLivingBase

/**
 * Logic for blocks which have a specific direction facing.
 *
 * @author mDiyo
 * @author Sunstrike
 */
trait IFacingLogic {

  def getRenderDirection: Byte

  def getForgeDirection: ForgeDirection

  @Deprecated
  def setDirection(side: Int)

  @Deprecated
  def setDirection(yaw: Float, pitch: Float, player: EntityLivingBase)

}

package mantle.blocks.abstracts

import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.List

/**
 * Slab version of InventoryBlock.
 *
 * @author mDiyo
 * @author Sunstrike
 */
abstract class InventorySlab(id:Int, mat:Material) extends InventoryBlock(id, mat) {

  override def renderAsNormalBlock: Boolean = false

  override def isOpaqueCube: Boolean = false

  override def shouldSideBeRendered(world: IBlockAccess, x: Int, y: Int, z: Int, side: Int): Boolean = {
    if (side > 1) return super.shouldSideBeRendered(world, x, y, z, side)
    val meta: Int = world.getBlockMetadata(x, y, z)
    val top: Boolean = (meta | 8) == 1
    if ((top && side == 0) || (!top && side == 1)) return true
    super.shouldSideBeRendered(world, x, y, z, side)
  }

  override def addCollisionBoxesToList(world: World, x: Int, y: Int, z: Int, axisalignedbb: AxisAlignedBB, arraylist: List[_], entity: Entity) {
    setBlockBoundsBasedOnState(world, x, y, z)
    super.addCollisionBoxesToList(world, x, y, z, axisalignedbb, arraylist, entity)
  }

  override def setBlockBoundsForItemRender() {
    setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F)
  }

  override def setBlockBoundsBasedOnState(world: IBlockAccess, x: Int, y: Int, z: Int) {
    val meta: Int = world.getBlockMetadata(x, y, z) / 8
    val minY: Float = if (meta == 1) 0.5F else 0.0F
    val maxY: Float = if (meta == 1) 1.0F else 0.5F
    setBlockBounds(0.0F, minY, 0F, 1.0F, maxY, 1.0F)
  }

  override def onBlockPlaced(par1World: World, blockX: Int, blockY: Int, blockZ: Int, side: Int, clickX: Float, clickY: Float, clickZ: Float, metadata: Int): Int = {
    if (side == 1) return metadata
    if (side == 0 || clickY >= 0.5F) return metadata | 8
    metadata
  }

  override def damageDropped(meta: Int): Int = meta % 8

}
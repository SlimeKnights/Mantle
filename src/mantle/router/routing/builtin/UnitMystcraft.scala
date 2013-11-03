package mantle.router.routing.builtin

import mantle.router.routing.{MantleMessage, IPipelineUnit}
import net.minecraft.nbt.NBTTagCompound
import cpw.mods.fml.common.event.FMLInterModComms

/**
 * Mystcraft pipeline unit
 *
 * @author Sunstrike <sunstrike@azurenode.net>
 */
object UnitMystcraft extends IPipelineUnit {

  def unitName = "Mystcraft Compat"

  def handleMessage(msg: MantleMessage) {
    if (msg.getMessage.equals("myst_fluidSymbol")) {
      msg.getAttachment match {
        case (fName:String, rarity:Float, grammarWeight:Float, blockInstability:Float) =>
          val nbt = new NBTTagCompound()
          val fluidSym = new NBTTagCompound()
          fluidSym.setString("fluidname", fName)
          fluidSym.setFloat("rarity", rarity)
          fluidSym.setFloat("grammarweight", grammarWeight)
          fluidSym.setFloat("instabilityPerBlock", blockInstability)
          nbt.setCompoundTag("fluidsymbol", fluidSym)
          sendMystMessage("fluidsymbol", nbt)
      }
    }
  }

  private def sendMystMessage(msg: String, nbt: NBTTagCompound) {
    FMLInterModComms.sendMessage("Mystcraft", msg, nbt)
  }

}

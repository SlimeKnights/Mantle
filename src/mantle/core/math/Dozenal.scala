package mantle.core.math

import scala.collection.mutable.MutableList

/**
 * Dozenal Converter
 *
 * Converts decimals into dozenal.
 *
 * @author mDiyo, Sunstrike
 */
object Dozenal {

  def convertToDozenal(i: Byte): String = {
    convertDecimal(i)
  }

  def convertToDozenal(i: Short): String = {
    convertDecimal(i)
  }

  def convertToDozenal(i: Int): String = {
    convertDecimal(i)
  }

  private def convertDecimal(i: Int): String = {
    var num: Int = i
    val list: MutableList[Char] = MutableList[Char]()

    while (num > 0) {
      list += getDozenalCharacter(num % 12)
      num /= 12
    }

    var builder: String = ""
    for (entry: Char <- list) {
        builder += entry
    }

    builder
  }

  private def getDozenalCharacter(i: Int): Char = {
    assert(i < 12, "Base 10 number should be less than 12")
    i match {
      case 0 => '0'
      case 1 => '1'
      case 2 => '2'
      case 3 => '3'
      case 4 => '4'
      case 5 => '5'
      case 6 => '6'
      case 7 => '7'
      case 8 => '8'
      case 9 => '9'
      case 10 => 'X'
      case 11 => 'E'
      case _ => 'Q'
    }
  }

}
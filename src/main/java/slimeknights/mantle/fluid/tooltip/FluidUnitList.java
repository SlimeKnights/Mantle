package slimeknights.mantle.fluid.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.fluid.FluidPredicate;

import java.util.List;

/**
 * Represents a list of tooltip unit types for a fluid
 */
public record FluidUnitList(IJsonPredicate<Fluid> fluid, List<FluidUnit> units) {
  public static final RecordLoadable<FluidUnitList> LOADABLE = RecordLoadable.create(
    FluidPredicate.LOADER.defaultField("fluid", FluidPredicate.NONE, FluidUnitList::fluid),
    FluidUnit.LOADABLE.list(1).requiredField("units", FluidUnitList::units),
    FluidUnitList::new);

  /**
   * Checks if this matches the given fluid
   */
  public boolean matches(Fluid fluid) {
    return this.fluid.matches(fluid);
  }

  /**
   * Applies the text of all child units
   */
  public int getText(List<Component> tooltip, int amount) {
    if (units != null) {
      for (FluidUnit unit : units) {
        amount = unit.getText(tooltip, amount);
      }
    }
    return amount;
  }
}

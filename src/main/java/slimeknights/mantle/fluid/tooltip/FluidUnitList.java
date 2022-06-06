package slimeknights.mantle.fluid.tooltip;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a list of tooltip unit types for a fluid
 */
@SuppressWarnings("ClassCanBeRecord") // needed in GSON
@RequiredArgsConstructor
public class FluidUnitList {
  @Nullable
  private final TagKey<Fluid> tag;
  private final List<FluidUnit> units;

  /**
   * Checks if this matches the given fluid
   */
  public boolean matches(Fluid fluid) {
    return this.tag != null && RegistryHelper.contains(this.tag, fluid);
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

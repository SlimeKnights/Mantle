package slimeknights.mantle.fluid.texture;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/** Client logic for {@link slimeknights.mantle.fluid.PotionFluidType} */
public class ClientPotionFluidType extends ClientTextureFluidType {
  public ClientPotionFluidType(FluidType type) {
    super(type);
  }

  /**
   * Gets the color of the fluid.
   * @param stack  Fluid stack instance
   * @return  Color for the fluid
   */
  @Override
  public int getTintColor(FluidStack stack) {
    return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
  }
}

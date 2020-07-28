package slimeknights.mantle.registration.object;

import lombok.AllArgsConstructor;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Object containing registry entries for a fluid
 * @param <F>  Fluid class
 */
@SuppressWarnings("WeakerAccess")
@AllArgsConstructor
public class FluidObject<F extends ForgeFlowingFluid> implements Supplier<F>, IItemProvider {
  private final Supplier<? extends F> still;
  private final Supplier<? extends F> flowing;
  @Nullable
  private final Supplier<? extends FlowingFluidBlock> block;

  /**
   * Gets the still form of this fluid
   * @return  Still form
   */
  public F getStill() {
    return Objects.requireNonNull(still.get(), "Fluid object missing still fluid");
  }

  @Override
  public F get() {
    return getStill();
  }

  /**
   * Gets the flowing form of this fluid
   * @return  flowing form
   */
  public F getFlowing() {
    return Objects.requireNonNull(flowing.get(), "Fluid object missing flowing fluid");
  }

  /**
   * Gets the block form of this fluid
   * @return  Block form
   */
  @Nullable
  public FlowingFluidBlock getBlock() {
    if (block == null) {
      return null;
    }
    return block.get();
  }

  /**
   * Gets the bucket form of this fluid
   * @return  Bucket form
   */
  @Override
  public Item asItem() {
    return still.get().getFilledBucket();
  }
}

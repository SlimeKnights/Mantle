package slimeknights.mantle.registration;

import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;

import java.util.function.Supplier;

/**
 * Fluid properties builder class, since the Forge one requires too many suppliers that we do not have access to yet
 */
@Accessors(fluent = true)
public class FluidBuilder {
  private final FluidAttributes attributes;
  private boolean canMultiply = false;
  private Supplier<? extends Item> bucket;
  private Supplier<? extends FluidBlock> block;
  private int slopeFindDistance = 4;
  private int levelDecreasePerBlock = 1;
  private float explosionResistance = 1;
  private int tickRate = 5;

  public FluidBuilder(FluidAttributes attributes) {
    this.attributes = attributes;
  }

  /** Sets {@code canMultiply} to true */
  public FluidBuilder canMultiply() {
    canMultiply = true;
    return this;
  }

  public void setCanMultiply(boolean canMultiply) {
    this.canMultiply = canMultiply;
  }

  public void setBucket(Supplier<? extends Item> bucket) {
    this.bucket = bucket;
  }

  public void setBlock(Supplier<? extends FluidBlock> block) {
    this.block = block;
  }

  public void setSlopeFindDistance(int slopeFindDistance) {
    this.slopeFindDistance = slopeFindDistance;
  }

  public void setLevelDecreasePerBlock(int levelDecreasePerBlock) {
    this.levelDecreasePerBlock = levelDecreasePerBlock;
  }

  public void setExplosionResistance(float explosionResistance) {
    this.explosionResistance = explosionResistance;
  }

  public void setTickRate(int tickRate) {
    this.tickRate = tickRate;
  }

  /**
   * Builds Forge fluid properties from this builder
   * @param still    Still fluid supplier
   * @param flowing  Flowing supplier
   * @return  Forge fluid properties
   */
  public ForgeFlowingFluid.Properties build(Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
    //TODO: look at lint and see how i did custom fluids
    ForgeFlowingFluid.Properties properties = new ForgeFlowingFluid.Properties(still, flowing, this.attributes)
        .slopeFindDistance(this.slopeFindDistance)
        .levelDecreasePerBlock(this.levelDecreasePerBlock)
        .explosionResistance(this.explosionResistance)
        .tickRate(this.tickRate)
        .block(this.block)
        .bucket(this.bucket);
    if (this.canMultiply) {
      properties.canMultiply();
    }
    return properties;
  }
}

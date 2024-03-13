package slimeknights.mantle.registration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.util.function.Supplier;

/**
 * Fluid properties' builder class, since the Forge one requires too many suppliers that we do not have access to yet
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FluidBuilder<T extends FluidBuilder<T>> {
  protected Supplier<? extends Item> bucket;
  protected Supplier<? extends LiquidBlock> block;
  private int slopeFindDistance = 4;
  private int levelDecreasePerBlock = 1;
  private float explosionResistance = 1;
  private int tickRate = 5;

  /** Creates a new builder instance */
  public static FluidBuilder<?> create() {
    return new FluidBuilder<>();
  }

  /** Returns self casted to the given type */
  @SuppressWarnings("unchecked")
  private T self() {
    return (T) this;
  }

  /** Sets the supplier for the bucket */
  public T bucket(Supplier<? extends Item> value) {
    this.bucket = value;
    return self();
  }

  /** Sets the supplier for the bucket */
  public T block(Supplier<? extends LiquidBlock> value) {
    this.block = value;
    return self();
  }


  /* Basic properties */

  public T slopeFindDistance(int value) {
    this.slopeFindDistance = value;
    return self();
  }

  public T levelDecreasePerBlock(int value) {
    this.levelDecreasePerBlock = value;
    return self();
  }

  public T explosionResistance(int value) {
    this.explosionResistance = value;
    return self();
  }

  /** Sets the slope find distance */
  public T tickRate(int value) {
    this.tickRate = value;
    return self();
  }

  /**
   * Builds Forge fluid properties from this builder
   * @param still    Still fluid supplier
   * @param flowing  Flowing supplier
   * @return  Forge fluid properties
   */
  public ForgeFlowingFluid.Properties build(Supplier<? extends FluidType> type, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
    return new ForgeFlowingFluid.Properties(type, still, flowing)
        .slopeFindDistance(this.slopeFindDistance)
        .levelDecreasePerBlock(this.levelDecreasePerBlock)
        .explosionResistance(this.explosionResistance)
        .tickRate(this.tickRate)
        .block(this.block)
        .bucket(this.bucket);
  }
}

package slimeknights.mantle.registration.adapter;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties;
import net.minecraft.core.Registry;
import slimeknights.mantle.registration.DelayedSupplier;
import slimeknights.mantle.registration.FluidBuilder;

import java.util.function.Function;

/**
 * Registry adapter for registering fluids
 */
@SuppressWarnings("unused")
public class FluidRegistryAdapter extends RegistryAdapter<Fluid> {
  /** @inheritDoc */
  public FluidRegistryAdapter(Registry<Fluid> registry) {
    super(registry);
  }

  /** @inheritDoc */
  public FluidRegistryAdapter(Registry<Fluid> registry, String modId) {
    super(registry, modId);
  }

  /**
   * Registers a new fluid with both still and flowing variants
   * @param builder   Fluid properties builder, contains the fluid type and possible bucket form
   * @param still     Still constructor
   * @param flowing   Flowing constructor
   * @param name      Fluid name
   * @param <F>       Fluid type
   * @return  Still fluid instance
   */
  public <F extends BaseFlowingFluid> F register(FluidBuilder<?> builder, Function<Properties, F> still, Function<Properties,F> flowing, String name) {
    // have to create still and flowing later, as the props need these suppliers
    DelayedSupplier<Fluid> stillDelayed = new DelayedSupplier<>();
    DelayedSupplier<Fluid> flowingDelayed = new DelayedSupplier<>();

    // create props with the suppliers
    Properties props = builder.build(builder.getType(), stillDelayed, flowingDelayed);

    // create fluids now that we have props
    // TODO: should we be using holders?
    F stillFluid = register(still.apply(props), name);
    stillDelayed.setSupplier(() -> stillFluid);
    F flowingFluid = register(flowing.apply(props), "flowing_" + name);
    flowingDelayed.setSupplier(() -> flowingFluid);

    // return the final nice object
    return stillFluid;
  }

  /**
   * Registers a fluid using default constructors
   * @param builder  Fluid builder
   * @param name     Fluid name
   * @return  Still fluid
   */
  public BaseFlowingFluid register(FluidBuilder<?> builder, String name) {
    return register(builder, BaseFlowingFluid.Source::new, BaseFlowingFluid.Flowing::new, name);
  }
}

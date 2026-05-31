package slimeknights.mantle.data.predicate.fluid;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.predicate.IJsonPredicate;

import java.util.Set;

/** Predicate that checks if the fluid type matches any of a set of fluid types */
public record FluidTypePredicate(Set<FluidType> types) implements FluidPredicate {
  public static final RecordLoadable<FluidTypePredicate> LOADER = RecordLoadable.create(Loadables.FLUID_TYPE.set().requiredField("fluid_types", FluidTypePredicate::types), FluidTypePredicate::new);

  public FluidTypePredicate(FluidType... types) {
    this(ImmutableSet.copyOf(types));
  }

  @Override
  public boolean matches(Fluid fluid) {
    return types.contains(fluid.getFluidType());
  }

  @Override
  public RecordLoadable<? extends IJsonPredicate<Fluid>> getLoader() {
    return LOADER;
  }
}

package slimeknights.mantle.data.predicate.fluid;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.RegistryPredicateRegistry;
import slimeknights.mantle.util.RegistryHelper;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Size and data independent way to condition on fluids.
 * Allows for more advance combinations than standard ingredients at the cost of being unable to list values.
 * @see net.neoforged.neoforge.fluids.crafting.FluidIngredient
 */
public interface FluidPredicate extends IJsonPredicate<Fluid> {
  /** Predicate that matches all fluids */
  FluidPredicate ANY = simple(fluid -> true);
  /** Predicate that matches no fluids */
  FluidPredicate NONE = simple(fluid -> false);
  /** Loader for fluid predicates */
  RegistryPredicateRegistry<Fluid,Fluid> LOADER = new RegistryPredicateRegistry<>("Fluid Predicate", ANY, NONE, Loadables.FLUID, Function.identity(), "fluids", Loadables.FLUID_TAG, RegistryHelper::contains);

  @Override
  default IJsonPredicate<Fluid> inverted() {
    return LOADER.invert(this);
  }

  /** Creates a new predicate singleton */
  static FluidPredicate simple(Predicate<Fluid> predicate) {
    return SingletonLoader.singleton(loader -> new FluidPredicate() {
      @Override
      public boolean matches(Fluid fluid) {
        return predicate.test(fluid);
      }

      @Override
      public RecordLoadable<? extends FluidPredicate> getLoader() {
        return loader;
      }
    });
  }


  /* Simple */
  /** Checks if the fluid is a source, as opposed to a flowing fluid. */
  FluidPredicate SOURCE = simple(fluid -> fluid.isSource(fluid.defaultFluidState()));
  /** Checks if the fluid has a bucket form. */
  FluidPredicate HAS_BUCKET = simple(fluid -> fluid.getBucket() != Items.AIR);
  /** Checks if the fluid is lighter than air, typically meaning it flows upwards. */
  FluidPredicate LIGHTER_THAN_AIR = simple(fluid -> fluid.getFluidType().isLighterThanAir());


  /* Helper methods */

  /** Creates a fluid set predicate */
  static IJsonPredicate<Fluid> set(Fluid... fluids) {
    return LOADER.setOf(fluids);
  }

  /** Creates a tag predicate */
  static IJsonPredicate<Fluid> tag(TagKey<Fluid> tag) {
    return LOADER.tag(tag);
  }

  /** Creates an and predicate */
  @SafeVarargs
  static IJsonPredicate<Fluid> and(IJsonPredicate<Fluid>... predicates) {
    return LOADER.and(List.of(predicates));
  }

  /** Creates an or predicate */
  @SafeVarargs
  static IJsonPredicate<Fluid> or(IJsonPredicate<Fluid>... predicates) {
    return LOADER.or(List.of(predicates));
  }
}

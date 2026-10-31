package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.network.codec.MantleStreamCodecs;
import slimeknights.mantle.recipe.ingredient.IngredientHelper;

import java.util.List;

/**
 * Wrapper around {@link HolderSet} with amount with a display {@link FluidStack} getter.
 * Designed as an alternative to {@link net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient} for non-component sensitive usages.
 * @see net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient
 */
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "of")
public class FluidStackSet {
  /** Empty instance, useful for defaults */
  public static final FluidStackSet EMPTY = FluidStackSet.of(HolderSet.empty(), 0);
  /** Loader instance */
  public static final RecordLoadable<FluidStackSet> LOADABLE = RecordLoadable.create(
    Loadables.FLUID_SET.requiredField("fluid", s -> s.fluids),
    IntLoadable.FROM_ONE.requiredField("amount", s -> s.amount),
    FluidStackSet::new);
  /** Map codec instance */
  public static final MapCodec<FluidStackSet> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MantleCodecs.FLUID_SET.fieldOf("fluid").forGetter(s -> s.fluids),
    ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(s -> s.amount)
  ).apply(instance, FluidStackSet::new));
  /**
   * Codec instance.
   * @see #MAP_CODEC
   */
  public static final Codec<FluidStackSet> CODEC = MAP_CODEC.codec();
  /** Stream codec instance */
  public static final StreamCodec<RegistryFriendlyByteBuf,FluidStackSet> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.FLUID_SET, s -> s.fluids,
    ByteBufCodecs.VAR_INT, s -> s.amount,
    FluidStackSet::new);

  @Getter
  private final HolderSet<Fluid> fluids;
  @Getter
  private final int amount;
  private List<FluidStack> display;

  /** Creates a set for the given fluid */
  @SuppressWarnings("deprecation")
  public static FluidStackSet of(Fluid fluid, int amount) {
    return of(HolderSet.direct(fluid.builtInRegistryHolder()), amount);
  }

  /** Creates a set for the given fluids */
  public static FluidStackSet of(List<Fluid> fluid, int amount) {
    return of(IngredientHelper.fluidSet(fluid), amount);
  }

  /** Creates a set for the given tag */
  public static FluidStackSet of(TagKey<Fluid> fluid, int amount) {
    return of(IngredientHelper.fluidSet(fluid), amount);
  }

  /** {@return true if the given fluid is in the set} */
  public boolean contains(Holder<Fluid> fluid) {
    return this.fluids.contains(fluid);
  }

  /** {@return true if the given fluid is in the set} */
  @SuppressWarnings("deprecation")
  public boolean contains(Fluid fluid) {
    return this.fluids.contains(fluid.builtInRegistryHolder());
  }


  /** Gets the cached display fluids. */
  public List<FluidStack> display() {
    if (display == null) {
      display = fluids.stream().map(fluid -> new FluidStack(fluid, amount)).toList();
    }
    return display;
  }


  /* Default fields mostly */

  @Override
  public boolean equals(@Nullable Object object) {
    if (this == object) return true;
    return object instanceof FluidStackSet other && this.amount == other.amount && this.fluids.equals(other.fluids);
  }

  @Override
  public int hashCode() {
    return 31 * this.amount + this.fluids.hashCode();
  }
}

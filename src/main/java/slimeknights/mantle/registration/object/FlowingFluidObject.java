package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.recipe.ingredient.IngredientHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Object containing registry entries for a fluid with a flowing form.
 * @param <F>  Fluid class
 */
@SuppressWarnings("WeakerAccess")
public class FlowingFluidObject<F extends FlowingFluid> extends FluidObject<F> {
  /** Tag in the mod namespace, exact match */
  @Getter @Nonnull
  private final TagKey<Fluid> localTag;
  private final DeferredHolder<Fluid,F> flowing;
  @Nullable
  private final Supplier<? extends LiquidBlock> block;

  /** Main constructor */
  public FlowingFluidObject(ResourceLocation id, @Nullable String tagName, Supplier<? extends FluidType> type, DeferredHolder<Fluid,F> still, DeferredHolder<Fluid,F> flowing, @Nullable Supplier<? extends LiquidBlock> block) {
    super(id, tagName, type, still);
    this.localTag = FluidTags.create(id);
    this.flowing = flowing;
    this.block = block;
  }

  /**
   * Gets the still form of this fluid. Alias for {@link #get()} for code readability.
   * @return  Still form
   * @see #get()
   */
  public F getStill() {
    return get();
  }

  /** {@return  flowing form} */
  public F getFlowing() {
    return flowing.get();
  }

  /** {@return holder of the flowing form} */
  public Holder<Fluid> getFlowingHolder() {
    return flowing;
  }

  /**
   * Gets the block form of this fluid
   * @return  Block form
   */
  @Nullable
  public LiquidBlock getBlock() {
    if (block == null) {
      return null;
    }
    return block.get();
  }


  /* Datagen helpers */

  /** Gets the primary tag used for recipes with this object */
  public TagKey<Fluid> getTag() {
    return commonTag != null ? commonTag : localTag;
  }

  @Override
  public HolderSet<Fluid> holderSet() {
    return IngredientHelper.fluidSet(getTag());
  }

  @Override
  public FluidIngredient ingredient() {
    return FluidIngredient.tag(getTag());
  }
}

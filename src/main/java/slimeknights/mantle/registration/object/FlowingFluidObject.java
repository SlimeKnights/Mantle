package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
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
  private final Supplier<? extends F> flowing;
  @Nullable
  private final Supplier<? extends LiquidBlock> block;

  /** Main constructor */
  public FlowingFluidObject(ResourceLocation id, String tagName, Supplier<? extends FluidType> type, Supplier<? extends F> still, Supplier<? extends F> flowing, @Nullable Supplier<? extends LiquidBlock> block) {
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
  public LiquidBlock getBlock() {
    if (block == null) {
      return null;
    }
    return block.get();
  }

  @Override
  public FluidIngredient ingredient(int amount, boolean commonTag) {
    return FluidIngredient.of(commonTag ? getForgeTag() : getLocalTag(), amount);
  }
}

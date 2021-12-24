package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.Item;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags.IOptionalNamedTag;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Object containing registry entries for a fluid
 * @param <F>  Fluid class
 */
@SuppressWarnings("WeakerAccess")
public class FluidObject<F extends ForgeFlowingFluid> implements Supplier<F>, ItemLike {
  /** Fluid name, used for tag creation */
  @Getter @Nonnull
  protected final ResourceLocation id;
  /** Tag in the mod namespace, exact match */
  @Getter @Nonnull
  private final IOptionalNamedTag<Fluid> localTag;
  /** Tag in the forge namespace, crafting equivalence */
  @Getter @Nonnull
  private final IOptionalNamedTag<Fluid> forgeTag;
  private final Supplier<? extends F> still;
  private final Supplier<? extends F> flowing;
  @Nullable
  private final Supplier<? extends LiquidBlock> block;

  /** Main constructor */
  public FluidObject(ResourceLocation id, String tagName, Supplier<? extends F> still, Supplier<? extends F> flowing, @Nullable Supplier<? extends LiquidBlock> block) {
    this.id = id;
    this.localTag = FluidTags.createOptional(id);
    this.forgeTag = FluidTags.createOptional(new ResourceLocation("forge", tagName));
    this.still = still;
    this.flowing = flowing;
    this.block = block;
  }

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
  public LiquidBlock getBlock() {
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
    return still.get().getBucket();
  }
}

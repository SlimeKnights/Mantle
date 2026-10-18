package slimeknights.mantle.registration.object;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.ingredient.IngredientHelper;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Object containing registry entries for a fluid with no flowing form.
 * @param <F>  Fluid class
 * @see FlowingFluidObject
 */
@SuppressWarnings("WeakerAccess")
public class FluidObject<F extends Fluid> implements Supplier<F>, ItemLike, IdAwareObject {
  /** Fluid name, used for tag creation */
  @Getter @Accessors(fluent = true)
  protected final ResourceLocation id;

  /** Tag in the forge namespace, crafting equivalence */
  @Getter @Nullable
  protected final TagKey<Fluid> commonTag;
  private final Supplier<? extends FluidType> type;
  private final DeferredHolder<Fluid,F> still;

  /** Main constructor */
  public FluidObject(ResourceLocation id, @Nullable String tagName, Supplier<? extends FluidType> type, DeferredHolder<Fluid,F> still) {
    this.id = id;
    this.commonTag = tagName == null ? null : FluidTags.create(Mantle.commonResource(tagName));
    this.type = type;
    this.still = still;
  }

  /** Gets the fluid type for this object */
  public FluidType getType() {
    return type.get();
  }

  /** {@return still form of fluids} */
  @Override
  public F get() {
    return still.get();
  }

  /** Gets the holder of the still form. */
  public Holder<Fluid> getHolder() {
    return still;
  }

  /**
   * Gets the bucket form of this fluid.
   * @return  Bucket form, or null if no bucket
   * @see #asItem()
   */
  @Nullable
  public Item getBucket() {
    Item bucket = still.get().getBucket();
    if (bucket == Items.AIR) {
      return null;
    }
    return bucket;
  }

  /**
   * Gets the bucket form of this fluid
   * @return  Bucket form, or air if no bucket
   * @see #getBucket()
   */
  @Override
  public Item asItem() {
    return still.get().getBucket();
  }

  /** Gets a holder set using either the fluid tag or the still value. */
  public HolderSet<Fluid> holderSet() {
    if (commonTag != null) {
      return IngredientHelper.fluidSet(commonTag);
    }
    return HolderSet.direct(still);
  }

  /**
   * Creates a sizeless ingredient from this object
   * @return  Ingredient instance
   */
  public FluidIngredient ingredient() {
    if (commonTag != null) {
      return FluidIngredient.tag(commonTag);
    }
    return FluidIngredient.single(get());
  }

  /**
   * Creates a sized ingredient from this object
   * @param amount     Ingredient amount
   * @return  Ingredient instance
   */
  public SizedFluidIngredient ingredient(int amount) {
    return new SizedFluidIngredient(ingredient(), amount);
  }

  /**
   * Creates a recipe result from this object
   * @param amount     Result amount
   * @return  Result instance
   */
  public FluidOutput result(int amount) {
    if (commonTag != null) {
      return FluidOutput.fromTag(commonTag, amount);
    }
    return FluidOutput.fromFluid(get(), amount);
  }
}

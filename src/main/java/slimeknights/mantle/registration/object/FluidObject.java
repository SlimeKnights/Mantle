package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Object containing registry entries for a fluid with no flowing form.
 * @param <F>  Fluid class
 * @see FlowingFluidObject
 */
@SuppressWarnings("WeakerAccess")
public class FluidObject<F extends Fluid> implements Supplier<F>, ItemLike {
  /** Fluid name, used for tag creation */
  @Getter @Nonnull
  protected final ResourceLocation id;

  /** Tag in the forge namespace, crafting equivalence */
  @Getter @Nonnull
  private final TagKey<Fluid> forgeTag;
  private final Supplier<? extends FluidType> type;
  private final Supplier<? extends F> still;

  /** Main constructor */
  public FluidObject(ResourceLocation id, String tagName, Supplier<? extends FluidType> type, Supplier<? extends F> still) {
    this.id = id;
    this.forgeTag = FluidTags.create(new ResourceLocation("forge", tagName));
    this.type = type;
    this.still = still;
  }

  /** Gets the fluid type for this object */
  public FluidType getType() {
    return type.get();
  }

  /**
   * Gets the still form of this fluid
   * @return  Still form
   */
  @Override
  public F get() {
    return Objects.requireNonNull(still.get(), "Fluid object missing still fluid");
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

  /**
   * Creates an ingredient from this object
   * @param amount     Ingredient amount
   * @param commonTag  If true, matches the common tag, if false matches just this object
   * @return  Ingredient instance
   */
  public FluidIngredient ingredient(int amount, boolean commonTag) {
    if (commonTag) {
      return FluidIngredient.of(get(), amount);
    }
    return FluidIngredient.of(getForgeTag(), amount);
  }
}

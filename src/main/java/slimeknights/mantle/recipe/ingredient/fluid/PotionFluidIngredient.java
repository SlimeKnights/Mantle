package slimeknights.mantle.recipe.ingredient.fluid;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.network.MantleStreamCodecs;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.List;
import java.util.stream.Stream;

/**
 * Simple fluid ingredient checking for a fluid with a specific potion
 * @see slimeknights.mantle.recipe.ingredient.item.PotionIngredient
 */
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class PotionFluidIngredient extends FluidIngredient {
  public static final MapCodec<PotionFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    BuiltInRegistries.POTION.holderByNameCodec().fieldOf("potion").forGetter(i -> i.potion),
    IngredientFluids.CODEC.forGetter(i -> i.fluids)
  ).apply(instance, PotionFluidIngredient::new));
  public static final StreamCodec<RegistryFriendlyByteBuf, PotionFluidIngredient> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.POTION, i -> i.potion,
    IngredientFluids.STREAM_CODEC, i -> i.fluids,
    PotionFluidIngredient::new);

  private final Holder<Potion> potion;
  private final IngredientFluids fluids;

  /** Creates a potion ingredient matching a list of fluids */
  public static FluidIngredient of(Holder<Potion> potion, List<Fluid> fluids) {
    return new PotionFluidIngredient(potion, IngredientFluids.of(fluids));
  }

  /** Creates a potion ingredient matching a list of fluids */
  public static FluidIngredient of(Holder<Potion> potion, Fluid... fluids) {
    return of(potion, List.of(fluids));
  }

  /** Creates a potion ingredient matching a tag */
  public static FluidIngredient of(Holder<Potion> potion, TagKey<Fluid> tag) {
    return new PotionFluidIngredient(potion, IngredientFluids.of(tag));
  }

  /** Creates a potion ingredient using the standard potion tag */
  public static FluidIngredient of(Holder<Potion> potion) {
    return of(potion, MantleTags.Fluids.POTION);
  }

  @Override
  public FluidIngredientType<PotionFluidIngredient> getType() {
    return MantleRecipes.POTION_FLUID_INGREDIENT.get();
  }

  @Override
  public boolean test(FluidStack stack) {
    // must match a fluid, and potion must match
    return fluids.test(stack) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(potion);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  protected Stream<FluidStack> generateStacks() {
    PotionContents contents = new PotionContents(potion);
    return fluids.getAllFluids().stream().map(fluid -> {
      FluidStack stack = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
      stack.set(DataComponents.POTION_CONTENTS, contents);
      return stack;
    });
  }
}

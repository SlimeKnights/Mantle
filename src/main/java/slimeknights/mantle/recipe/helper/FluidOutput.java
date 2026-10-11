package slimeknights.mantle.recipe.helper;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.Streamable;
import slimeknights.mantle.data.loadable.common.CodecStreamable;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.mapping.EitherRecordLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.DataComponentHelper;

import java.util.Optional;

/**
 * Class representing a fluid stack output. Supports both direct stacks and tag output, behaving like {@link net.neoforged.neoforge.fluids.crafting.FluidIngredient} used for output.
 * @see TagPreference
 * @see ItemOutput
 */
@SuppressWarnings("unused")  // API
public abstract sealed class FluidOutput extends StackOutput<Fluid,FluidStack> {
  /** Empty instance */
  public static final FluidOutput EMPTY = new OfStack(FluidStack.EMPTY);

  /* Constructors */

  /**
   * Creates a new output for the given stack
   * @param stack  Stack
   * @return  Output
   */
  public static FluidOutput fromStack(FluidStack stack) {
    if (stack.isEmpty()) {
      return EMPTY;
    }
    return new OfStack(stack);
  }

  /**
   * Creates a new output for the given item
   * @param fluid  Fluid
   * @param amount Fluid size
   * @return  Output
   */
  public static FluidOutput fromFluid(Fluid fluid, int amount) {
    return fromStack(new FluidStack(fluid, amount));
  }

  /**
   * Creates a new output for the given tag
   * @param tag         Tag
   * @param amount      Stack amount
   * @param components  Stack components
   * @return Output
   */
  public static FluidOutput fromTag(TagKey<Fluid> tag, int amount, DataComponentPatch components) {
    return new OfTagPreference(tag, amount, components);
  }

  /**
   * Creates a new output for the given tag
   * @param tag   Tag
   * @param amount Stack amount
   * @return Output
   */
  public static FluidOutput fromTag(TagKey<Fluid> tag, int amount) {
    return fromTag(tag, amount, DataComponentPatch.EMPTY);
  }


  /* Main methods */

  @Override
  public final FluidStack copy() {
    return get().copy();
  }


  /* Field getters */

  /** {@return the size of the output} Does not resolve the stack. */
  public abstract int getAmount();

  @Override
  public boolean isEmpty() {
    return getAmount() <= 0;
  }


  /** Class for an output that is just a stack */
  @RequiredArgsConstructor
  private static final class OfStack extends FluidOutput {
    private final FluidStack stack;

    @Override
    public Either<Fluid, TagKey<Fluid>> getValueOrTag() {
      return Either.left(stack.getFluid());
    }

    @Override
    public FluidStack get() {
      return stack;
    }

    @Override
    public int getAmount() {
      return stack.getAmount();
    }

    @Override
    public DataComponentPatch getComponents() {
      return stack.getComponentsPatch();
    }
  }

  /** Class for an output from a tag preference */
  @RequiredArgsConstructor
  private static final class OfTagPreference extends FluidOutput {
    private final TagKey<Fluid> tag;
    @Getter
    private final int amount;
    @Getter
    private final DataComponentPatch components;
    private FluidStack cachedResult = null;

    @Override
    public Either<Fluid, TagKey<Fluid>> getValueOrTag() {
      return Either.right(tag);
    }

    @Override
    public FluidStack get() {
      // cache the result from the tag preference to save effort, especially helpful if the tag becomes invalid
      // this object should only exist in recipes so no need to invalidate the cache
      if (cachedResult == null) {
        // if the preference is empty, do not cache it.
        // This should only happen if someone scans recipes before tag are computed in which case we cache the wrong result.
        // We protect against empty tags in our recipes via conditions.
        Optional<Fluid> preference = TagPreference.getPreference(tag);
        if (preference.isEmpty()) {
          return FluidStack.EMPTY;
        }
        cachedResult = new FluidStack(preference.orElseThrow(), amount);
        if (!components.isEmpty()) {
          cachedResult.applyComponents(components);
        }
      }
      return cachedResult;
    }
  }


  /* Serializing */

  /** Creates from the codec */
  private static FluidOutput fromCodec(Either<Fluid,TagKey<Fluid>> fluidOrTag, DataComponentPatch components, int count) {
    if (count <= 0) {
      return EMPTY;
    }
    return fluidOrTag.map(
      fluid -> fromStack(DataComponentHelper.makeStack(fluid, count, components)),
      tag -> fromTag(tag, count, components)
    );
  }


  /* Codecs */

  /** Prepared item tag field */
  private static final MapCodec<TagKey<Fluid>> FLUID_TAG_CODEC = MantleCodecs.FLUID_TAG.fieldOf("tag");

  /** Codec matching a non-empty stack with amount. */
  public static final Codec<FluidOutput> REQUIRED_CODEC = RecordCodecBuilder.create(instance ->
    codecStart(instance, NeoForgeExtraCodecs.xor(MantleCodecs.NON_EMPTY_FLUID.fieldOf("fluid"), FLUID_TAG_CODEC))
      .and(ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidOutput::getAmount)).apply(instance, FluidOutput::fromCodec));
  /** Codec matching a stack that may be empty with amount. */
  public static final Codec<FluidOutput> OPTIONAL_CODEC = RecordCodecBuilder.create(instance ->
    codecStart(instance, NeoForgeExtraCodecs.xor(BuiltInRegistries.FLUID.byNameCodec().optionalFieldOf("fluid", Fluids.EMPTY), FLUID_TAG_CODEC))
      .and(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("amount").orElse(1).forGetter(FluidOutput::getAmount)).apply(instance, FluidOutput::fromCodec));

  /** Stream codec instance syncing a required stack directly */
  public static final StreamCodec<RegistryFriendlyByteBuf,FluidOutput> REQUIRED_STREAM_CODEC = FluidStack.STREAM_CODEC.map(FluidOutput::fromStack, FluidOutput::get);
  /** Stream codec instance syncing an optional stack directly */
  public static final StreamCodec<RegistryFriendlyByteBuf,FluidOutput> OPTIONAL_STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC.map(FluidOutput::fromStack, FluidOutput::get);


  /* Loadables */
  /** Item tag or required item */
  private static final RecordField<Either<Fluid,TagKey<Fluid>>,FluidOutput> REQUIRED_ITEM_OR_TAG_FIELD = new EitherRecordLoadable<>("fluid", Loadables.NON_EMPTY_FLUID, "tag", Loadables.FLUID_TAG).directField(FluidOutput::getValueOrTag);
  /** Item tag or optional item */
  private static final RecordField<Either<Fluid,TagKey<Fluid>>,FluidOutput> OPTIONAL_ITEM_OR_TAG_FIELD = new EitherRecordLoadable<>("fluid", Loadables.FLUID, "tag", Loadables.FLUID_TAG, Either.left(Fluids.EMPTY)).directField(FluidOutput::getValueOrTag);
  /** Network logic for required stacks */
  private static final Streamable<FluidOutput> REQUIRED_STREAMABLE = new CodecStreamable<>(REQUIRED_STREAM_CODEC);
  /** Network logic for optional stacks */
  private static final Streamable<FluidOutput> OPTIONAL_STREAMABLE = new CodecStreamable<>(OPTIONAL_STREAM_CODEC);

  /** Loadable matching a non-empty stack with amount. */
  public static final RecordLoadable<FluidOutput> REQUIRED_LOADABLE = RecordLoadable.create(REQUIRED_ITEM_OR_TAG_FIELD, COMPONENTS_FIELD, IntLoadable.FROM_ONE.requiredField("amount", FluidOutput::getAmount), FluidOutput::fromCodec).withNetwork(new CodecStreamable<>(REQUIRED_STREAM_CODEC));
  /** Loadable matching a stack that may be empty with amount. */
  public static final RecordLoadable<FluidOutput> OPTIONAL_LOADABLE = RecordLoadable.create(OPTIONAL_ITEM_OR_TAG_FIELD, COMPONENTS_FIELD, IntLoadable.FROM_ZERO.requiredField("amount", FluidOutput::getAmount), FluidOutput::fromCodec).withNetwork(new CodecStreamable<>(OPTIONAL_STREAM_CODEC));

  @Deprecated(forRemoval = true)
  public static class Loadable {
    /** @deprecated use {@link #OPTIONAL_LOADABLE} */
    @Deprecated(forRemoval = true)
    public static final RecordLoadable<FluidOutput> OPTIONAL = OPTIONAL_LOADABLE;
    /** @deprecated use {@link #REQUIRED_LOADABLE} */
    @Deprecated(forRemoval = true)
    public static final RecordLoadable<FluidOutput> REQUIRED = REQUIRED_LOADABLE;
  }
}

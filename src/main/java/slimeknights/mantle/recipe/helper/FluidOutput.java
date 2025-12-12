package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.FluidStackLoadable;
import slimeknights.mantle.data.loadable.common.NBTLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class representing a fluid stack output. Supports both direct stacks and tag output, behaving like {@link slimeknights.mantle.recipe.ingredient.FluidIngredient} used for output
 */
public abstract class FluidOutput implements Supplier<FluidStack> {
  /** Empty instance */
  public static final FluidOutput EMPTY = new OfStack(FluidStack.EMPTY);


  /**
   * Gets the fluid output of this recipe. This should not be modified
   * @return  fluid output
   */
  @Override
  public abstract FluidStack get();

  /**
   * Gets a copy of the result stack
   * @return  fluid output
   */
  public final FluidStack copy() {
    return get().copy();
  }

  /**
   * Gets the size of the fluid. Equivelent to {@link #get()} then {@link FluidStack#getAmount()} but saves resolving the fluid.
   */
  public abstract int getAmount();

  /** Checks if the contents are empty without resolving the stack */
  public boolean isEmpty() {
    return getAmount() <= 0;
  }

  /** Gets the tag for this output. Will be {@code null} if this is not a tag output. */
  @Nullable
  public TagKey<Fluid> getTag() {
    return null;
  }

  /**
   * Writes this output to JSON
   * @return  Json element
   */
  @Deprecated(forRemoval = true)
  public JsonObject serialize() {
    JsonObject json = new JsonObject();
    serialize(json);
    return json;
  }

  /** Writes this output to JSON */
  public abstract void serialize(JsonObject json);

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
    return new OfFluid(fluid, amount);
  }

  /**
   * Creates a new output for the given tag
   * @param tag   Tag
   * @param amount Stack amount
   * @param nbt    Stack NBT
   * @return Output
   */
  public static FluidOutput fromTag(TagKey<Fluid> tag, int amount, @Nullable CompoundTag nbt) {
    return new OfTagPreference(tag, amount, nbt);
  }

  /**
   * Creates a new output for the given tag
   * @param tag   Tag
   * @param amount Stack amount
   * @return Output
   */
  public static FluidOutput fromTag(TagKey<Fluid> tag, int amount) {
    return fromTag(tag, amount, null);
  }

  /**
   * Writes this output to the packet buffer
   * @param buffer  Packet buffer instance
   */
  public void write(FriendlyByteBuf buffer) {
    buffer.writeFluidStack(get());
  }

  /**
   * Reads an item output from the packet buffer
   * @param buffer  Buffer instance
   * @return  Item output
   */
  public static FluidOutput read(FriendlyByteBuf buffer) {
    return fromStack(buffer.readFluidStack());
  }

  /** Class for an output that is just an item, simplifies NBT for serializing as vanilla forces NBT to be set for tools and forge goes through extra steps when NBT is set */
  @RequiredArgsConstructor
  private static class OfFluid extends FluidOutput {
    private final Fluid fluid;
    @Getter
    private final int amount;
    private FluidStack cachedStack;

    @Override
    public FluidStack get() {
      if (cachedStack == null) {
        cachedStack = new FluidStack(fluid, amount);
      }
      return cachedStack;
    }

    @Override
    public void serialize(JsonObject json) {
      if (amount > 0) {
        json.add("fluid", Loadables.FLUID.serialize(this.fluid));
      }
      json.addProperty("amount", amount);
    }
  }

  /** Class for an output that is just a stack */
  @RequiredArgsConstructor
  private static class OfStack extends FluidOutput {
    private final FluidStack stack;

    @Override
    public FluidStack get() {
      return stack;
    }

    @Override
    public int getAmount() {
      return stack.getAmount();
    }

    @Override
    public void serialize(JsonObject json) {
      FluidStackLoadable.OPTIONAL_STACK_NBT.serialize(stack, json);
    }
  }

  /** Class for an output from a tag preference */
  @RequiredArgsConstructor
  private static class OfTagPreference extends FluidOutput {
    @Getter
    private final TagKey<Fluid> tag;
    @Getter
    private final int amount;
    @Nullable
    private final CompoundTag nbt;
    private FluidStack cachedResult = null;

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
        cachedResult = new FluidStack(preference.orElseThrow(), amount, nbt);
      }
      return cachedResult;
    }

    @Override
    public void serialize(JsonObject json) {
      if (amount > 0) {
        json.addProperty("tag", tag.location().toString());
      }
      json.addProperty("amount", amount);
      if (amount > 0 && nbt != null) {
        json.add("nbt", NBTLoadable.ALLOW_STRING.serialize(nbt));
      }
    }
  }

  /** Loadable logic for an FluidOutput */
  public enum Loadable implements RecordLoadable<FluidOutput> {
    /** Loadable for an output that may be empty with any size */
    OPTIONAL(false),
    /** Loadable for an output that may not be empty with any size */
    REQUIRED(true);

    private final boolean nonEmpty;
    private final RecordLoadable<FluidStack> stack;
    Loadable(boolean nonEmpty) {
      this.nonEmpty = nonEmpty;
      // figure out the stack serializer to use based on the two parameters
      // we always do NBT, just those that vary
      if (nonEmpty) {
        this.stack = FluidStackLoadable.REQUIRED_STACK_NBT;
      } else {
        this.stack = FluidStackLoadable.OPTIONAL_STACK_NBT;
      }
    }

    @Override
    public FluidOutput deserialize(JsonObject json, TypedMap context) {
      if (json.has("tag")) {
        return fromTag(
          Loadables.FLUID_TAG.getIfPresent(json, "tag", context),
          IntLoadable.FROM_ONE.getIfPresent(json, "amount", context),
          NBTLoadable.ALLOW_STRING.getOrDefault(json, "nbt", null));
      }
      return fromStack(stack.deserialize(json, context));
    }

    @Override
    public void serialize(FluidOutput output, JsonObject json) {
      if (nonEmpty && output.isEmpty()) {
        throw new IllegalArgumentException("FluidOutput cannot be empty for this recipe");
      }
      output.serialize(json);
    }

    @Override
    public FluidOutput decode(FriendlyByteBuf buffer, TypedMap context) {
      return fromStack(stack.decode(buffer, context));
    }

    @Override
    public void encode(FriendlyByteBuf buffer, FluidOutput object) {
      stack.encode(buffer, object.get());
    }


    /* Defaulting behavior */

    /** Gets the output, defaulting to empty. Note this will not stop you from getting empty with a non-empty loadable, thats on you for weirdly calling. */
    public FluidOutput getOrEmpty(JsonObject parent, String key) {
      return getOrDefault(parent, key, FluidOutput.EMPTY);
    }

    /** Creates a field defaulting to empty */
    public <P> LoadableField<FluidOutput,P> emptyField(String key, boolean serializeDefault, Function<P,FluidOutput> getter) {
      return defaultField(key, FluidOutput.EMPTY, serializeDefault, getter);
    }

    /** Creates a field defaulting to empty that does not serialize if empty */
    public <P> LoadableField<FluidOutput,P> emptyField(String key, Function<P,FluidOutput> getter) {
      return emptyField(key, false, getter);
    }
  }
}

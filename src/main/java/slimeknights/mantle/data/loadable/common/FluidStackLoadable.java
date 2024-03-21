package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.EncoderException;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Loadable for a fluid stack */
@RequiredArgsConstructor
public enum FluidStackLoadable implements RecordLoadable<FluidStack> {
  /** Loads a non-empty fluid stack, ignoring NBT */
  NON_EMPTY(false, true),
  /** Loads a non-empty fluid stack, including NBT */
  NON_EMPTY_NBT(true, true),
  /** Loads a fluid stack that may be empty, ignoring NBT */
  EMPTY(false, false),
  /** Loads a fluid stack that may be empty, including NBT */
  EMPTY_NBT(true, false);

  /** If true, we read NBT */
  private final boolean readNBT;
  /** If true, we disallow reading empty stacks */
  private final boolean disallowEmpty;

  @Override
  public FluidStack convert(JsonElement element, String key) {
    if (!disallowEmpty && element.isJsonNull()) {
      return FluidStack.EMPTY;
    }
    return RecordLoadable.super.convert(element, key);
  }

  /** Deserializes this stack from an object */
  @Override
  public FluidStack deserialize(JsonObject json) {
    Fluid fluid = Fluids.EMPTY;
    // if we disallow empty, force parsing the fluid so we get a missing field error
    // item field is optional if we allow empty
    if (json.has("fluid") || disallowEmpty) {
      fluid = Loadables.FLUID.getAndDeserialize(json, "fluid");
    }
    // air may come from the default or the registry, either is disallowed if we disallow empty
    if (fluid == Fluids.EMPTY) {
      if (disallowEmpty) {
        throw new JsonSyntaxException("FluidStack may not be empty");
      }
      return FluidStack.EMPTY;
    }
    // we handle empty via item, so amount is not even considered, thus amount of 0 is invalid
    int amount = GsonHelper.getAsInt(json, "amount");
    if (amount <= 0) {
      throw new JsonSyntaxException("FluidStack amount must greater than 0");
    }
    CompoundTag tag = null;
    if (readNBT && json.has("nbt")) {
      tag = NBTLoadable.ALLOW_STRING.convert(json.get("nbt"), "nbt");
    }
    return new FluidStack(fluid, amount, tag);
  }

  @Override
  public FluidStack getAndDeserialize(JsonObject parent, String key) {
    if (!disallowEmpty && !parent.has(key)) {
      return FluidStack.EMPTY;
    }
    return RecordLoadable.super.getAndDeserialize(parent, key);
  }

  @Override
  public JsonElement serialize(FluidStack stack) {
    if (stack.isEmpty()) {
      if (disallowEmpty) {
        throw new IllegalArgumentException("FluidStack must not be empty");
      }
      return JsonNull.INSTANCE;
    }
    return RecordLoadable.super.serialize(stack);
  }

  @Override
  public void serialize(FluidStack stack, JsonObject json) {
    if (stack.isEmpty()) {
      if (disallowEmpty) {
        throw new IllegalArgumentException("FluidStack must not be empty");
      }
      return;
    }
    json.add("fluid", Loadables.FLUID.serialize(stack.getFluid()));
    json.addProperty("amount", stack.getAmount());
    CompoundTag tag = readNBT ? stack.getTag() : null;
    if (tag != null) {
      json.add("nbt", NBTLoadable.ALLOW_STRING.serialize(tag));
    }
  }

  @Override
  public FluidStack fromNetwork(FriendlyByteBuf buffer) {
    return FluidStack.readFromPacket(buffer);
  }

  @Override
  public void toNetwork(FluidStack stack, FriendlyByteBuf buffer) throws EncoderException {
    stack.writeToPacket(buffer);
  }
}

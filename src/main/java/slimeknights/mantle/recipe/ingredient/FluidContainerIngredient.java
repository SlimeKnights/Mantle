package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Ingredient that matches a container of fluid.
 */
@SuppressWarnings("unused")  // API
public class FluidContainerIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = Mantle.getResource("fluid_container");

  public static final MapCodec<FluidContainerIngredient> CODEC = new MapCodec<>() {
    @Override
    public <O> DataResult<FluidContainerIngredient> decode(DynamicOps<O> ops, MapLike<O> input) {
      try {
        JsonObject json = new JsonObject();
        for (var entry : input.entries().toList()) {
          String key = ops.getStringValue(entry.getFirst()).result().orElse(null);
          if (key != null) {
            JsonElement value = ops.convertTo(JsonOps.INSTANCE, entry.getSecond());
            json.add(key, value);
          }
        }
        return DataResult.success(parse(json));
      } catch (RuntimeException e) {
        return DataResult.error(() -> e.getMessage());
      }
    }

    @Override
    public <O> RecordBuilder<O> encode(FluidContainerIngredient input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
      JsonObject json = input.toJson();
      for (var entry : json.entrySet()) {
        prefix.add(entry.getKey(), JsonOps.INSTANCE.convertTo(ops, entry.getValue()));
      }
      return prefix;
    }

    @Override
    public String toString() {
      return ID.toString();
    }
  };

  public static final IngredientType<FluidContainerIngredient> TYPE = new IngredientType<>(CODEC);

  /** Ingredient to use for matching */
  private final FluidIngredient fluidIngredient;
  /** Internal ingredient to display the ingredient recipe viewers */
  @Nullable
  private final Ingredient display;
  private final int hashCode;
  
  protected FluidContainerIngredient(FluidIngredient fluidIngredient, @Nullable Ingredient display) {
    this.fluidIngredient = fluidIngredient;
    this.display = display;
    this.hashCode = Objects.hash(fluidIngredient.serialize(), display);
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient, Ingredient display) {
    return new FluidContainerIngredient(ingredient, display);
  }

  /** Creates an instance from a fluid ingredient with no display, not recommended */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient) {
    return new FluidContainerIngredient(ingredient, null);
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromFluid(FluidObject<?> fluid) {
    return fromIngredient(fluid.ingredient(FluidType.BUCKET_VOLUME), Ingredient.of(fluid));
  }

  private static FluidContainerIngredient parse(JsonObject json) {
    FluidIngredient fluidIngredient;
    // if we have fluid and its not a primitive, then its nested
    if (json.has("fluid") && !json.get("fluid").isJsonPrimitive()) {
      fluidIngredient = FluidIngredient.LOADABLE.getIfPresent(json, "fluid");
    } else {
      fluidIngredient = FluidIngredient.LOADABLE.convert(json, "fluid");
    }
    Ingredient display = null;
    if (json.has("display")) {
      JsonElement element = JsonHelper.getElement(json, "display");
      display = Ingredient.CODEC_NONEMPTY.parse(JsonOps.INSTANCE, element)
                                         .resultOrPartial(error -> { throw new IllegalArgumentException(error); })
                                         .orElseThrow(() -> new IllegalArgumentException("Invalid display ingredient"));
    }
    return new FluidContainerIngredient(fluidIngredient, display);
  }

  /** Serializes this ingredient to JSON fields (excluding the {@code type} field). */
  private JsonObject toJson() {
    JsonElement element = fluidIngredient.serialize();
    JsonObject json;
    if (element.isJsonObject()) {
      json = element.getAsJsonObject();
    } else {
      json = new JsonObject();
      json.add("fluid", element);
    }
    if (display != null) {
      json.add("display", serializeIngredient(display));
    }
    return json;
  }

  /** Serializes an ingredient to JSON using the 1.21 codec system. */
  private static JsonElement serializeIngredient(Ingredient ingredient) {
    return Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, ingredient)
                                    .resultOrPartial(error -> Mantle.logger.error("Failed to serialize ingredient for {}: {}", ID, error))
                                    .orElseThrow(() -> new IllegalStateException("Failed to serialize ingredient"));
  }

  @Override
  public boolean test(ItemStack stack) {
    // first, must have a fluid capability
    if (stack.isEmpty()) {
      return false;
    }

    IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (handler == null || handler.getTanks() != 1) {
      return false;
    }

    FluidStack contained = handler.getFluidInTank(0);
    if (contained.isEmpty() || !fluidIngredient.test(contained.getFluid())) {
      return false;
    }
    Fluid fluid = contained.getFluid();
    int amount = fluidIngredient.getAmount(fluid);
    if (amount != contained.getAmount()) {
      return false;
    }

    ItemStack copy = ItemHandlerHelper.copyStackWithSize(stack, 1);
    IFluidHandlerItem copyHandler = copy.getCapability(Capabilities.FluidHandler.ITEM);
    if (copyHandler == null) {
      return false;
    }

    FluidStack drained = copyHandler.drain(amount, FluidAction.EXECUTE);
    return drained.getFluid() == fluid && drained.getAmount() == amount && ItemStack.matches(stack.getCraftingRemainder(), copyHandler.getContainer());
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (display != null) {
      ItemStack[] displayItems = display.getItems();
      if (displayItems.length > 0) {
        return Arrays.stream(displayItems);
      }
    }

    // Best-effort fallback: show buckets for the matched fluids.
    // This avoids the ingredient being treated as "accidentally empty" in viewers/recipe loading.
    var buckets = fluidIngredient.getFluids().stream()
                                 .map(stack -> new ItemStack(stack.getFluid().getBucket()))
                                 .filter(stack -> !stack.isEmpty())
                                 .toList();
    if (!buckets.isEmpty()) {
      return buckets.stream();
    }

    ItemStack fallback = new ItemStack(Items.BARRIER);
    fallback.set(DataComponents.CUSTOM_NAME, Component.literal("Fluid Container"));
    return Stream.of(fallback);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TYPE;
  }

  /** Gets the fluid ingredient */
  public FluidIngredient getFluidIngredient() {
    return fluidIngredient;
  }

  /** Gets the display ingredient, may be null */
  @Nullable
  public Ingredient getDisplay() {
    return display;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FluidContainerIngredient other) || hashCode != other.hashCode) {
      return false;
    }
    if (!fluidIngredient.serialize().equals(other.fluidIngredient.serialize())) {
      return false;
    }
    return Objects.equals(display, other.display);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}

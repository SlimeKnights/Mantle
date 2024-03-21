package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.util.JsonHelper;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

// TODO: fluid ingredient loadable
@SuppressWarnings("unused")
public abstract class FluidIngredient {
  /** Empty fluid ingredient, matches nothing */
  public static final FluidIngredient EMPTY = new Empty();
  /** Fluid json serializer instance */
  public static Serializer SERIALIZER = new Serializer();

  /** Cached list of display fluids */
  private List<FluidStack> displayFluids;

  /**
   * Checks if the given fluid matches this ingredient
   * @param fluid  Fluid to check
   * @return  True if the fluid matches
   */
  public abstract boolean test(Fluid fluid);

  /**
   * Gets the amount of the given fluid needed for the recipe
   * @param fluid  Fluid to check
   * @return  Amount of the fluid needed
   */
  public abstract int getAmount(Fluid fluid);

  /**
   * Checks if the given fluid stack argument matches this ingredient
   * @param stack  Fluid stack to check
   * @return  True if the fluid matches this ingredient and the amount is equal or greater than this
   */
  public boolean test(FluidStack stack) {
    Fluid fluid = stack.getFluid();
    return stack.getAmount() >= getAmount(fluid) && test(stack.getFluid());
  }

  /**
   * Gets a list of fluid stacks contained in this ingredient for display
   * @return  List of fluid stacks for this ingredient
   */
  public List<FluidStack> getFluids() {
    if (displayFluids == null) {
      displayFluids = getAllFluids().stream().filter(stack -> {
        Fluid fluid = stack.getFluid();
        return fluid.isSource(fluid.defaultFluidState());
      }).collect(Collectors.toList());
    }
    return displayFluids;
  }

  /**
   * Gets a list of fluid stacks contained in this ingredient for display, may include flowing fluids
   * @return  List of fluid stacks for this ingredient
   */
  protected abstract List<FluidStack> getAllFluids();

  /**
   * Serializes the Fluid Ingredient into JSON
   * @return  FluidIngredient JSON
   */
  public abstract JsonElement serialize();

  /**
   * Writes the ingredient into the packet buffer
   * @param buffer Packet buffer instance
   */
  public void write(FriendlyByteBuf buffer) {
    Collection<FluidStack> fluids = getAllFluids();
    buffer.writeInt(fluids.size());
    for (FluidStack stack : fluids) {
      buffer.writeUtf(Registry.FLUID.getKey(stack.getFluid()).toString());
      buffer.writeInt(stack.getAmount());
    }
  }


  /*
   * Instance creation
   */

  /**
   * Creates a new ingredient using the given fluid and amount
   * @param fluid   Fluid to check
   * @param amount  Minimum fluid amount
   * @return  Fluid ingredient for this fluid
   */
  public static FluidIngredient of(Fluid fluid, int amount) {
    return new FluidIngredient.FluidMatch(fluid, amount);
  }

  /**
   * Creates a new ingredient using the given fluidstack
   * @param stack  Fluid stack
   * @return  Fluid ingredient for this fluid stack
   */
  public static FluidIngredient of(FluidStack stack) {
    return of(stack.getFluid(), stack.getAmount());
  }

  /**
   * Creates a new fluid ingredient from the given tag
   * @param fluid   Fluid tag
   * @param amount  Minimum fluid amount
   * @return  Fluid ingredient from a tag
   */
  public static FluidIngredient of(TagKey<Fluid> fluid, int amount) {
    return new FluidIngredient.TagMatch(fluid, amount);
  }

  /**
   * Creates a new compound ingredient from the given list of ingredients
   * @param ingredients  Ingredient list
   * @return  Compound ingredient
   */
  public static FluidIngredient of(FluidIngredient... ingredients) {
    return new FluidIngredient.Compound(ingredients);
  }


  /*
   * JSON deserializing
   */

  /**
   * Deserializes the fluid ingredient from JSON
   * @param parent  Parent containing the fluid JSON
   * @param name    Name of the key to fetch from the parent object
   * @return  Fluid ingredient instance
   * @throws JsonSyntaxException if syntax is invalid
   */
  public static FluidIngredient deserialize(JsonObject parent, String name) {
    return deserialize(parent, name, true);
  }

  /**
   * Deserializes the fluid ingredient from JSON
   * @param parent      Parent containing the fluid JSON
   * @param name        Name of the key to fetch from the parent object
   * @param allowEmpty  If true, empty ingredient may be returned
   * @return  Fluid ingredient instance
   * @throws JsonSyntaxException if syntax is invalid
   */
  public static FluidIngredient deserialize(JsonObject parent, String name, boolean allowEmpty) {
    return deserialize(JsonHelper.getElement(parent, name), name, allowEmpty);
  }

  /**
   * Deserializes the fluid ingredient from JSON
   * @param json  Json element instance
   * @param name  Name of the object for error messages
   * @return  Fluid ingredient instance
   * @throws JsonSyntaxException if syntax is invalid
   */
  public static FluidIngredient deserialize(JsonElement json, String name) {
    return deserialize(json, name, true);
  }

  /**
   * Deserializes the fluid ingredient from JSON
   * @param json        Json element instance
   * @param name        Name of the object for error messages
   * @param allowEmpty  If true, empty ingredient may be returned
   * @return  Fluid ingredient instance
   * @throws JsonSyntaxException if syntax is invalid
   */
  public static FluidIngredient deserialize(JsonElement json, String name, boolean allowEmpty) {
    // single ingredient object
    if (json.isJsonObject()) {
      return deserializeObject(json.getAsJsonObject(), allowEmpty);
    }

    // array
    if (json.isJsonArray()) {
      return Compound.deserialize(json.getAsJsonArray(), name);
    }

    throw new JsonSyntaxException("Fluid ingredient " + name + " must be either an object or array");
  }

  /**
   * Deserializes the fluid ingredient from JSON
   * @param json        JSON object
   * @param allowEmpty  If true, empty is a valid result
   * @return  Fluid Ingredient
   * @throws JsonSyntaxException if syntax is invalid
   */
  private static FluidIngredient deserializeObject(JsonObject json, boolean allowEmpty) {
    if (allowEmpty && json.entrySet().isEmpty()) {
      return EMPTY;
    }

    // fluid match
    if (json.has("fluid")) {
      // don't set both, obviously an error
      if (json.has("tag")) {
        throw new JsonSyntaxException("An ingredient entry is either a tag or an fluid, not both");
      }

      // parse a fluid
      return FluidMatch.deserialize(json);
    }

    // tag match
    if (json.has("tag")) {
      return TagMatch.deserialize(json);
    }

    throw new JsonSyntaxException("An ingredient entry needs either a tag or an fluid");
  }


  /*
   * Packet buffers
   */

  /**
   * Reads a fluid ingredient from the packet buffer
   * @param buffer  Buffer instance
   * @return  Fluid ingredient instance
   */
  public static FluidIngredient read(FriendlyByteBuf buffer) {
    int count = buffer.readInt();
    FluidIngredient[] ingredients = new FluidIngredient[count];
    for (int i = 0; i < count; i++) {
      Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(buffer.readUtf(32767)));
      if (fluid == null) {
        fluid = Fluids.EMPTY;
      }
      int amount = buffer.readInt();
      ingredients[i] = of(fluid, amount);
    }
    // if a single ingredient, do not wrap in compound
    if (count == 1) {
      return ingredients[0];
    }
    // compound for anything else
    return of(ingredients);
  }

  /**
   * Empty fluid ingredient, matches only empty fluid stacks
   */
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  private static class Empty extends FluidIngredient {
    @Override
    public boolean test(Fluid fluid) {
      return fluid == Fluids.EMPTY;
    }
    @Override
    public boolean test(FluidStack fluid) {
      return fluid.isEmpty();
    }

    @Override
    public int getAmount(Fluid fluid) {
      return 0;
    }

    @Override
    public List<FluidStack> getAllFluids() {
      return Collections.emptyList();
    }

    @Override
    public JsonElement serialize() {
      return new JsonObject();
    }
  }

  /**
   * Fluid ingredient that matches a single fluid
   */
  @AllArgsConstructor(access=AccessLevel.PRIVATE)
  private static class FluidMatch extends FluidIngredient {
    private final Fluid fluid;
    private final int amount;

    @Override
    public boolean test(Fluid fluid) {
      return fluid == this.fluid;
    }

    @Override
    public int getAmount(Fluid fluid) {
      return amount;
    }

    @Override
    public List<FluidStack> getAllFluids() {
      return Collections.singletonList(new FluidStack(fluid, amount));
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      object.addProperty("fluid", Registry.FLUID.getKey(fluid).toString());
      object.addProperty("amount", amount);
      return object;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
      // count
      buffer.writeInt(1);
      // single fluid
      buffer.writeUtf(Registry.FLUID.getKey(fluid).toString());
      buffer.writeInt(amount);
    }

    /**
     * Deserailizes the ingredient from JSON
     * @param json  JSON object
     * @return Fluid ingredient instance
     */
    private static FluidMatch deserialize(JsonObject json) {
      String fluidName = GsonHelper.getAsString(json, "fluid");
      Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
      if (fluid == null || fluid == Fluids.EMPTY) {
        throw new JsonSyntaxException("Unknown fluid '" + fluidName + "'");
      }
      int amount = GsonHelper.getAsInt(json, "amount");
      return new FluidMatch(fluid, amount);
    }
  }

  /**
   * Fluid ingredient that matches a tag
   */
  @AllArgsConstructor(access=AccessLevel.PRIVATE)
  private static class TagMatch extends FluidIngredient {
    private final TagKey<Fluid> tag;
    private final int amount;

    @Override
    public boolean test(Fluid fluid) {
      return fluid.is(tag);
    }

    @Override
    public int getAmount(Fluid fluid) {
      return amount;
    }

    @Override
    public List<FluidStack> getAllFluids() {
      return StreamSupport.stream(Registry.FLUID.getTagOrEmpty(tag).spliterator(), false)
                          .filter(Holder::isBound)
                          .map(fluid -> new FluidStack(fluid.value(), amount))
                          .toList();
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      object.addProperty("tag", this.tag.location().toString());
      object.addProperty("amount", amount);
      return object;
    }

    /**
     * Deseralizes the ingredient from JSON
     * @param json  JSON object
     * @return Fluid ingredient instance
     */
    private static TagMatch deserialize(JsonObject json) {
      TagKey<Fluid> tag = TagKey.create(Registry.FLUID_REGISTRY, JsonHelper.getResourceLocation(json, "tag"));
      int amount = GsonHelper.getAsInt(json, "amount");
      return new TagMatch(tag, amount);
    }
  }

  /**
   * Fluid ingredient that matches a list of ingredients
   */
  private static class Compound extends FluidIngredient {
    private final List<FluidIngredient> ingredients;
    private Compound(FluidIngredient[] ingredients) {
      this.ingredients = Arrays.asList(ingredients);
    }

    @Override
    public boolean test(Fluid fluid) {
      return ingredients.stream().anyMatch(ingredient -> ingredient.test(fluid));
    }

    @Override
    public boolean test(FluidStack stack) {
      return ingredients.stream().anyMatch(ingredient -> ingredient.test(stack));
    }

    @Override
    public int getAmount(Fluid fluid) {
      return ingredients.stream()
                        .filter(ingredient -> ingredient.test(fluid))
                        .mapToInt(ingredient -> ingredient.getAmount(fluid))
                        .findFirst()
                        .orElse(0);
    }

    @Override
    public List<FluidStack> getAllFluids() {
      return ingredients.stream()
                        .flatMap(ingredient -> ingredient.getFluids().stream())
                        .collect(Collectors.toList());
    }

    @Override
    public JsonElement serialize() {
      return ingredients.stream()
                        .map(FluidIngredient::serialize)
                        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    /**
     * Deserializes a compound ingredient from JSON
     * @param array  JSON array
     * @param name   Array key
     * @return  Compound fluid ingredient instance
     */
    private static Compound deserialize(JsonArray array, String name) {
      // size must be valid
      int size = array.size();
      if (size == 0) {
        throw new JsonSyntaxException("Fluid array cannot be empty, at least one fluid must be defined");
      }

      // parse all ingredients
      FluidIngredient[] ingredients = new FluidIngredient[size];
      for (int i = 0; i < size; i++) {
        // no reason to an array in an array
        ingredients[i] = deserializeObject(GsonHelper.convertToJsonObject(array.get(i), name + "[" + i + "]"), false);
      }
      return new Compound(ingredients);
    }
  }

  /** Json serializer for fluids */
  public static class Serializer implements JsonDeserializer<FluidIngredient>, JsonSerializer<FluidIngredient> {
    private Serializer() {}

    @Override
    public FluidIngredient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return FluidIngredient.deserialize(json, "ingredient");
    }

    @Override
    public JsonElement serialize(FluidIngredient src, Type typeOfSrc, JsonSerializationContext context) {
      return src.serialize();
    }
  }

  /**
   * Loadable logic for fluid ingredients
   */
  public enum Loadable implements slimeknights.mantle.data.loadable.Loadable<FluidIngredient> {
    ALLOW_EMPTY,
    DISALLOW_EMPTY;

    @Override
    public FluidIngredient getAndDeserialize(JsonObject parent, String key) {
      if (this == ALLOW_EMPTY && !parent.has(key)) {
        return FluidIngredient.EMPTY;
      }
      return slimeknights.mantle.data.loadable.Loadable.super.getAndDeserialize(parent, key);
    }

    @Override
    public FluidIngredient convert(JsonElement element, String key) throws JsonSyntaxException {
      return FluidIngredient.deserialize(element, key, this == ALLOW_EMPTY);
    }

    @Override
    public JsonElement serialize(FluidIngredient object) throws RuntimeException {
      if (object == EMPTY && this == DISALLOW_EMPTY) {
        throw new IllegalArgumentException("Empty ingredient not allowed");
      }
      return object.serialize();
    }

    @Override
    public FluidIngredient fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
      return FluidIngredient.read(buffer);
    }

    @Override
    public void toNetwork(FluidIngredient object, FriendlyByteBuf buffer) throws EncoderException {
      object.write(buffer);
    }
  }
}

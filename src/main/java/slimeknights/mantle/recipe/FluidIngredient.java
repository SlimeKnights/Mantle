package slimeknights.mantle.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import slimeknights.mantle.util.JsonHelper;

// TODO: move to ingredient package in 1.17
@SuppressWarnings("unused")
public abstract class FluidIngredient {
  /** Empty fluid ingredient, matches nothing */
  public static final FluidIngredient EMPTY = new Empty();

  /** Cached list of display fluids */
  private List<FluidVolume> displayFluids;

  /**
   * Checks if the given fluid matches this ingredient
   * @param fluid  Fluid to check
   * @return  True if the fluid matches
   */
  public abstract boolean test(FluidKey fluid);

  /**
   * Gets the amount of the given fluid needed for the recipe
   * @param fluid  Fluid to check
   * @return  Amount of the fluid needed
   */
  public abstract FluidAmount getAmount(FluidKey fluid);

  /**
   * Checks if the given fluid stack argument matches this ingredient
   * @param stack  Fluid stack to check
   * @return  True if the fluid matches this ingredient and the amount is equal or greater than this
   */
  public boolean test(FluidVolume stack) {
    FluidKey fluid = stack.getFluidKey();
    return stack.amount().isGreaterThanOrEqual(getAmount(fluid)) && test(fluid);
  }

  /**
   * Gets a list of fluid stacks contained in this ingredient for display
   * @return  List of fluid stacks for this ingredient
   */
  public List<FluidVolume> getFluids() {
    if (displayFluids == null) {
      displayFluids = new ArrayList<>(getAllFluids());
    }
    return displayFluids;
  }

  /**
   * Gets a list of fluid stacks contained in this ingredient for display, may include flowing fluids
   * @return  List of fluid stacks for this ingredient
   */
  protected abstract List<FluidVolume> getAllFluids();

  /**
   * Serializes the Fluid Ingredient into JSON
   * @return  FluidIngredient JSON
   */
  public abstract JsonElement serialize();

  /**
   * Writes the ingredient into the packet buffer
   * @param buffer Packet buffer instance
   */
  public void write(PacketByteBuf buffer) {
    Collection<FluidVolume> fluids = getAllFluids();
    buffer.writeInt(fluids.size());
    for (FluidVolume stack : fluids) {
      stack.fluidKey.toMcBuffer(buffer);
      stack.amount().toMcBuffer(buffer);
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
  public static FluidIngredient of(FluidKey fluid, FluidAmount amount) {
    return new FluidIngredient.FluidMatch(fluid, amount);
  }

  /**
   * Creates a new ingredient using the given FluidVolume
   * @param stack  Fluid stack
   * @return  Fluid ingredient for this fluid stack
   */
  public static FluidIngredient of(FluidVolume stack) {
    return of(stack.getFluidKey(), stack.amount());
  }

  /**
   * Creates a new fluid ingredient from the given tag
   * @param fluid   Fluid tag
   * @param amount  Minimum fluid amount
   * @return  Fluid ingredient from a tag
   */
  public static FluidIngredient of(Tag<Fluid> fluid, FluidAmount amount) {
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
    return deserialize(JsonHelper.getElement(parent, name), name);
  }

  /**
   * Deserializes the fluid ingredient from JSON
   * @param json  Json element instance
   * @param name  Name of the object for error messages
   * @return  Fluid ingredient instance
   * @throws JsonSyntaxException if syntax is invalid
   */
  public static FluidIngredient deserialize(JsonElement json, String name) {
    // single ingredient object
    if (json.isJsonObject()) {
      return deserializeObject(json.getAsJsonObject());
    }

    // array
    if (json.isJsonArray()) {
      return Compound.deserialize(json.getAsJsonArray(), name);
    }

    throw new JsonSyntaxException("Fluid ingredient " + name + " must be either an object or array");
  }

  /**
   * Deserializes the fluid ingredient from JSON
   * @param json  JSON object
   * @return  Fluid Ingredient
   * @throws JsonSyntaxException if syntax is invalid
   */
  private static FluidIngredient deserializeObject(JsonObject json) {
    if (json.entrySet().isEmpty()) {
      return EMPTY;
    }

    // fluid match
    if (json.has("name")) {
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
  public static FluidIngredient read(PacketByteBuf buffer) {
    int count = buffer.readInt();
    FluidIngredient[] ingredients = new FluidIngredient[count];
    for (int i = 0; i < count; i++) {
      try {
        ingredients[i] = of(FluidVolume.fromMcBuffer(buffer));
      } catch (IOException e) {
        e.printStackTrace();
      }
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
  private static class Empty extends FluidIngredient {
    @Override
    public boolean test(FluidKey fluid) {
      return fluid.isEmpty();
    }
    @Override
    public boolean test(FluidVolume fluid) {
      return fluid.isEmpty();
    }

    @Override
    public FluidAmount getAmount(FluidKey fluid) {
      return FluidAmount.ZERO;
    }

    @Override
    public List<FluidVolume> getAllFluids() {
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
  private static class FluidMatch extends FluidIngredient {
    private final FluidKey fluid;
    private final FluidAmount amount;

    private FluidMatch(FluidKey fluid, FluidAmount amount) {
      this.fluid = fluid;
      this.amount = amount;
    }

    @Override
    public boolean test(FluidKey fluid) {
      return fluid.equals(this.fluid);
    }

    @Override
    public FluidAmount getAmount(FluidKey fluid) {
      return amount;
    }

    @Override
    public List<FluidVolume> getAllFluids() {
      return Collections.singletonList(fluid.withAmount(amount));
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      object.add("name", fluid.toJson());
      object.add("amount", amount.toJson());
      return object;
    }

    @Override
    public void write(PacketByteBuf buffer) {
      // count
      buffer.writeInt(1);
      // single fluid
      fluid.toMcBuffer(buffer);
      amount.toMcBuffer(buffer);
    }

    /**
     * Deserailizes the ingredient from JSON
     * @param json  JSON object
     * @return Fluid ingredient instance
     */
    private static FluidMatch deserialize(JsonObject json) {
      FluidKey fluid = FluidKey.fromJson(json.get("name").getAsJsonObject());
      if (fluid == null || fluid.isEmpty()) {
        throw new JsonSyntaxException("Unknown fluid '" + fluid.name + "'");
      }
      FluidAmount amount = FluidAmount.fromJson(json.get("amount"));
      return new FluidMatch(fluid, amount);
    }
  }

  /**
   * Fluid ingredient that matches a tag
   */
  private static class TagMatch extends FluidIngredient {
    private final Tag<Fluid> tag;
    private final FluidAmount amount;

    private TagMatch(Tag<Fluid> tag, FluidAmount amount) {
      this.tag = tag;
      this.amount = amount;
    }

    @Override
    public boolean test(FluidKey fluid) {
      return tag.contains(fluid.getRawFluid());
    }

    @Override
    public FluidAmount getAmount(FluidKey fluid) {
      return amount;
    }

    @Override
    public List<FluidVolume> getAllFluids() {
      return tag.values().stream().map(FluidKeys::get).map((fluid) -> fluid.withAmount(amount)).collect(Collectors.toList());
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      object.addProperty("tag", ServerTagManagerHolder.getTagManager().getFluids().getTagId(this.tag).toString());
      object.add("amount", amount.toJson());
      return object;
    }

    /**
     * Deseralizes the ingredient from JSON
     * @param json  JSON object
     * @return Fluid ingredient instance
     */
    private static TagMatch deserialize(JsonObject json) {
      String tagName = net.minecraft.util.JsonHelper.getString(json, "tag");
      Tag<Fluid> tag = ServerTagManagerHolder.getTagManager().getFluids().getTag(new Identifier(tagName));
      if (tag == null) {
        throw new JsonSyntaxException("Unknown fluid tag '" + tagName + "'");
      }
      FluidAmount amount = FluidAmount.fromJson(json.get("amount"));
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
    public boolean test(FluidKey fluid) {
      return ingredients.stream().anyMatch(ingredient -> ingredient.test(fluid));
    }

    @Override
    public boolean test(FluidVolume stack) {
      return ingredients.stream().anyMatch(ingredient -> ingredient.test(stack));
    }

    @Override
    public FluidAmount getAmount(FluidKey fluid) {
      return ingredients.stream()
                        .filter(ingredient -> ingredient.test(fluid))
                        .map(ingredient -> ingredient.getAmount(fluid))
                        .findFirst()
                        .orElse(FluidAmount.ZERO);
    }

    @Override
    public List<FluidVolume> getAllFluids() {
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
        ingredients[i] = deserializeObject(net.minecraft.util.JsonHelper.asObject(array.get(i), name + "[" + i + "]"));
      }
      return new Compound(ingredients);
    }
  }
}

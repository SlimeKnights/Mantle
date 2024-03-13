package slimeknights.mantle.recipe.ingredient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.util.JsonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Ingredient accepting an entity or an entity tag as an input
 */
public abstract class EntityIngredient implements Predicate<EntityType<?>> {
  /** Empty entity ingredient, matching nothing */
  public static final EntityIngredient EMPTY = new SetMatch(Collections.emptySet());

  /**
   * Gets a list of entity types matched by this ingredient
   * @return  List of types
   */
  public abstract Collection<EntityType<?>> getTypes();

  /**
   * Serializes this ingredient to JSON
   * @return  Json element of this ingredient
   */
  public abstract JsonElement serialize();

  /** Writes this ingredient to the packet buffer */
  public void write(FriendlyByteBuf buffer) {
    Collection<EntityType<?>> collection = getTypes();
    buffer.writeVarInt(collection.size());
    for (EntityType<?> type : collection) {
      buffer.writeRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES, type);
    }
  }

  /**
   * Creates an ingredient to match a single type
   */
  public static EntityIngredient of(EntityType<?> type) {
    return new Single(type);
  }

  /**
   * Creates an ingredient to match a set of types
   */
  public static EntityIngredient of(Set<EntityType<?>> set) {
    return new SetMatch(set);
  }

  /**
   * Creates an ingredient to match a set of types
   */
  public static EntityIngredient of(EntityType<?> ... types) {
    return of(ImmutableSet.copyOf(types));
  }

  /**
   * Creates an ingredient to match a tags
   */
  public static EntityIngredient of(TagKey<EntityType<?>> tag) {
    return new TagMatch(tag);
  }

  /**
   * Creates an ingredient from a list of ingredients
   */
  public static EntityIngredient of(EntityIngredient... ingredients) {
    return new Compound(Arrays.asList(ingredients));
  }

  /**
   * Reads an ingredient from the packet buffer
   * @param buffer  Buffer instance
   * @return  Ingredient instnace
   */
  public static EntityIngredient read(FriendlyByteBuf buffer) {
    int count = buffer.readVarInt();
    if (count == 1) {
      return new Single(buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES));
    }
    List<EntityType<?>> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      list.add(buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES));
    }
    return new SetMatch(ImmutableSet.copyOf(list));
  }

  /**
   * Finds an entity type for the given key
   * @param name  Entity type name
   * @return  Entity type
   */
  private static EntityType<?> findEntityType(ResourceLocation name) {
    if (ForgeRegistries.ENTITY_TYPES.containsKey(name)) {
      EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(name);
      if (type != null) {
        return type;
      }
    }
    throw new JsonSyntaxException("Invalid entity type " + name);
  }

  /**
   * Deserializes an ingredient from JSON
   * @param root  Json
   * @return  Ingredient
   */
  public static EntityIngredient deserialize(JsonElement root) {
    if (root.isJsonArray()) {
      JsonArray array = root.getAsJsonArray();
      ImmutableList.Builder<EntityIngredient> builder = ImmutableList.builder();
      for (JsonElement element : array) {
        builder.add(deserialize(element));
      }
      return new Compound(builder.build());
    }
    if (!root.isJsonObject()) {
      throw new JsonSyntaxException("Entity ingredient must be either an object or an array");
    }
    JsonObject json = root.getAsJsonObject();

    // type is just a name
    if (json.has("type")) {
      ResourceLocation name = new ResourceLocation(GsonHelper.getAsString(json, "type"));
      return new Single(findEntityType(name));
    }
    // tag is also a name
    if (json.has("tag")) {
      return new TagMatch(TagKey.create(Registry.ENTITY_TYPE_REGISTRY, JsonHelper.getResourceLocation(json, "tag")));
    }
    // types is a list
    if (json.has("types")) {
      List<EntityType<?>> types = JsonHelper.parseList(json, "types", (element, key) -> findEntityType(new ResourceLocation(GsonHelper.convertToString(element, key))));
      return new SetMatch(ImmutableSet.copyOf(types));
    }

    // missed all keys
    throw new JsonSyntaxException("Invalid entity type ingredient, must have 'type', 'types', or 'tag'");
  }

  /** Ingredient matching a single type */
  @RequiredArgsConstructor
  private static class Single extends EntityIngredient {
    private final EntityType<?> type;

    @Override
    public boolean test(EntityType<?> type) {
      return type == this.type;
    }

    @Override
    public List<EntityType<?>> getTypes() {
      return Collections.singletonList(type);
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      object.addProperty("type", Registry.ENTITY_TYPE.getKey(type).toString());
      return object;
    }
  }

  /** Ingredient that matches any entity from a set */
  @RequiredArgsConstructor
  private static class SetMatch extends EntityIngredient {
    private final Set<EntityType<?>> types;

    @Override
    public boolean test(EntityType<?> type) {
      return types.contains(type);
    }

    @Override
    public Set<EntityType<?>> getTypes() {
      return types;
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      JsonArray array = new JsonArray();
      for (EntityType<?> type : getTypes()) {
        array.add(Registry.ENTITY_TYPE.getKey(type).toString());
      }
      object.add("types", array);
      return object;
    }
  }

  /** Ingredient that matches any entity from a tag */
  @RequiredArgsConstructor
  private static class TagMatch extends EntityIngredient {
    private final TagKey<EntityType<?>> tag;
    private List<EntityType<?>> types;

    @Override
    public boolean test(EntityType<?> type) {
      return type.is(tag);
    }

    @Override
    public List<EntityType<?>> getTypes() {
      if (types == null) {
        types = StreamSupport.stream(Registry.ENTITY_TYPE.getTagOrEmpty(tag).spliterator(), false)
                             .filter(Holder::isBound)
                             .map(Holder::value)
                             .collect(Collectors.toList());
      }
      return types;
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      object.addProperty("tag", tag.location().toString());
      return object;
    }
  }

  /**
   * Ingredient combining multiple
   */
  @RequiredArgsConstructor
  private static class Compound extends EntityIngredient {
    private final List<EntityIngredient> ingredients;
    private List<EntityType<?>> allTypes;

    @Override
    public boolean test(EntityType<?> type) {
      for (EntityIngredient ingredient : ingredients) {
        if (ingredient.test(type)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public Collection<EntityType<?>> getTypes() {
      if (allTypes == null) {
        allTypes = ingredients.stream()
                              .flatMap(ingredient -> ingredient.getTypes().stream())
                              .distinct()
                              .collect(Collectors.toList());
      }
      return allTypes;
    }

    @Override
    public JsonElement serialize() {
      JsonArray array = new JsonArray();
      for (EntityIngredient ingredient : ingredients) {
        array.add(ingredient.serialize());
      }
      return array;
    }
  }
}

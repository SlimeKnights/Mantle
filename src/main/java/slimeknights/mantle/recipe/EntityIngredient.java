package slimeknights.mantle.recipe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import slimeknights.mantle.util.JsonHelper;

/**
 * Ingredient accepting an entity or an entity tag as an input
 * TODO: move to ingredient package in 1.17
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
  public void write(PacketByteBuf buffer) {
    Collection<EntityType<?>> collection = getTypes();
    buffer.writeVarInt(collection.size());
    for (EntityType<?> type : collection) {
      buffer.writeIdentifier(Registry.ENTITY_TYPE.getId(type));
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
  public static EntityIngredient of(Tag<EntityType<?>> tag) {
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
  public static EntityIngredient read(PacketByteBuf buffer) {
    int count = buffer.readVarInt();
    if (count == 1) {
      return new Single(Registry.ENTITY_TYPE.get(buffer.readIdentifier()));
    }
    List<EntityType<?>> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      list.add(Registry.ENTITY_TYPE.get(buffer.readIdentifier()));
    }
    return new SetMatch(ImmutableSet.copyOf(list));
  }

  /**
   * Finds an entity type for the given key
   * @param name  Entity type name
   * @return  Entity type
   */
  private static EntityType<?> findEntityType(Identifier name) {
    Optional<EntityType<?>> typeOptional = Registry.ENTITY_TYPE.getOrEmpty(name);

    if (typeOptional.isPresent()) return typeOptional.get();

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
      Identifier name = new Identifier(net.minecraft.util.JsonHelper.getString(json, "type"));
      return new Single(findEntityType(name));
    }
    // tag is also a name
    if (json.has("tag")) {
      Identifier name = new Identifier(net.minecraft.util.JsonHelper.getString(json, "tag"));
      Tag<EntityType<?>> tag = ServerTagManagerHolder.getTagManager().getEntityTypes().getTag(name);
      if (tag == null) {
        throw new JsonSyntaxException("Unknown entity type tag " + name);
      } else {
        return new TagMatch(tag);
      }
    }
    // types is a list
    if (json.has("types")) {
      List<EntityType<?>> types = JsonHelper.parseList(json, "types", (element, key) -> findEntityType(new Identifier(net.minecraft.util.JsonHelper.asString(element, key))));
      return new SetMatch(ImmutableSet.copyOf(types));
    }

    // missed all keys
    throw new JsonSyntaxException("Invalid entity type ingredient, must have 'type', 'types', or 'tag'");
  }

  /** Ingredient matching a single type */
  private static class Single extends EntityIngredient {
    private final EntityType<?> type;

    public Single(EntityType<?> type) {
      this.type = type;
    }

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
      object.addProperty("type", Objects.requireNonNull(Registry.ENTITY_TYPE.getId(type)).toString());
      return object;
    }
  }

  /** Ingredient that matches any entity from a set */
  private static class SetMatch extends EntityIngredient {
    private final Set<EntityType<?>> types;

    private SetMatch(Set<EntityType<?>> types) {
      this.types = types;
    }

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
        array.add(Objects.requireNonNull(Registry.ENTITY_TYPE.getId(type)).toString());
      }
      object.add("types", array);
      return object;
    }
  }

  /** Ingredient that matches any entity from a tag */
  private static class TagMatch extends EntityIngredient {
    private final Tag<EntityType<?>> tag;

    private TagMatch(Tag<EntityType<?>> tag) {
      this.tag = tag;
    }

    @Override
    public boolean test(EntityType<?> type) {
      return tag.contains(type);
    }

    @Override
    public List<EntityType<?>> getTypes() {
      return tag.values();
    }

    @Override
    public JsonElement serialize() {
      JsonObject object = new JsonObject();
      object.addProperty("tag", ServerTagManagerHolder.getTagManager().getEntityTypes().getTagId(tag).toString());
      return object;
    }
  }

  /**
   * Ingredient combining multiple
   */
  private static class Compound extends EntityIngredient {
    private final List<EntityIngredient> ingredients;
    private List<EntityType<?>> allTypes;

    private Compound(List<EntityIngredient> ingredients) {
      this.ingredients = ingredients;
    }

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

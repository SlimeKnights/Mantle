package slimeknights.mantle.recipe.ingredient;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.SimpleJsonSerializer;
import slimeknights.mantle.data.loadable.mapping.DispatchLoadable;
import slimeknights.mantle.data.loadable.mapping.DispatchLoadable.Builder;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Ingredient accepting an entity or an entity tag as an input
 */
@SuppressWarnings("unused")
public abstract class EntityIngredient implements Predicate<EntityType<?>> {
  /** Empty entity ingredient, matching nothing. This ingredient does not parse from JSON, use defaulting methods if you wish to use it */
  public static final EntityIngredient EMPTY = new Compound(Collections.emptyList());


  /* Loadables */

  /** Creates a builder with set and tag */
  private static Builder<EntityIngredient> loadableBuilder() {
    return DispatchLoadable.<EntityIngredient>builder().network(SET_MATCH).direct(SET_MATCH, "types").direct(ENTRY_MATCH, "type").direct(TAG_MATCH, "tag");
  }
  /** Loadable for a single value, notably not used for networking (though that probably would still work fine due to how EitherLoadable works) */
  private static final RecordLoadable<EntityIngredient> ENTRY_MATCH = RecordLoadable.create(Loadables.ENTITY_TYPE.requiredField("type", i -> {
    Set<EntityType<?>> types = i.getTypes();
    if (types.size() == 1) {
      return types.iterator().next();
    }
    throw new IllegalStateException("Cannot use entry match to serialize more than 1 entity");
  }), EntityIngredient::of);
  private static final SimpleJsonSerializer<EntityIngredient> SERIALIZER = EntityIngredient::serialize;
  /** Loadable for a set match */
  private static final RecordLoadable<EntityIngredient> SET_MATCH = RecordLoadable.create(Loadables.ENTITY_TYPE.set().requiredField("types", EntityIngredient::getTypes), EntityIngredient::of);
  /** Loadable for a tag match */
  private static final RecordLoadable<TagMatch> TAG_MATCH = RecordLoadable.create(Loadables.ENTITY_TYPE_TAG.requiredField("tag", t -> t.tag), TagMatch::new);
  /** Loadable disallows nested lists, just handles nested tags and sets */
  private static final Loadable<Compound> COMPOUND = EntityIngredient.loadableBuilder().build(SERIALIZER).list(2).flatXmap(Compound::new, c -> c.ingredients);
  /** Loadable for any fluid ingredient */
  public static final Loadable<EntityIngredient> LOADABLE = loadableBuilder().array(COMPOUND).build(SERIALIZER);

  /* Constructors */

  /**
   * Creates an ingredient to match a set of types
   */
  public static EntityIngredient of(Set<EntityType<?>> set) {
    if (set.isEmpty()) {
      return EMPTY;
    }
    return new SetMatch(set);
  }

  /** Creates an ingredient to match a set of types */
  public static EntityIngredient of(EntityType<?> ... types) {
    return of(ImmutableSet.copyOf(types));
  }

  /** Creates an ingredient to match a tags */
  public static EntityIngredient of(TagKey<EntityType<?>> tag) {
    return new TagMatch(tag);
  }

  /** Creates an ingredient from a list of ingredients */
  public static EntityIngredient of(EntityIngredient... ingredients) {
    return of(List.of(ingredients));
  }

  /** Creates an ingredient from a list of ingredients */
  private static EntityIngredient of(List<EntityIngredient> ingredients) {
    if (ingredients.isEmpty()) {
      return EMPTY;
    }
    if (ingredients.size() == 1) {
      return ingredients.getFirst();
    }
    return new Compound(ingredients);
  }


  /* Common methods */

  /**
   * Gets a set of entity types matched by this ingredient
   * @return  Set of types
   */
  public abstract Set<EntityType<?>> getTypes();

  /** Serializes this into JSON */
  protected abstract JsonElement serialize(TypedMap context);


  /* JEI */

  private List<EntityInput> display;
  private List<ItemStack> eggs;

  /** Gets the list of eggs matching this ingredient, used for display in JEI as it cannot do entity type */
  public List<EntityInput> getDisplay() {
    if (display == null) {
      display = EntityInput.wrap(getTypes());
    }
    return display;
  }

  /**
   * Gets the list of eggs matching this ingredient, used for focus links in JEI.
   * Note the list may contain null for any entities whose egg does not exist.
   */
  public List<@Nullable ItemStack> getEggs() {
    if (eggs == null) {
      // use getDisplay to guarantee order is the same, just in case
      eggs = getDisplay().stream().map(type -> {
        Item item = SpawnEggItem.byId(type.type);
        if (item == null) {
          return null;
        }
        return new ItemStack(item);
      }).toList();
    }
    return eggs;
  }



  /* Impls */

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
    protected JsonElement serialize(TypedMap context) {
      return (types.size() == 1 ? ENTRY_MATCH : SET_MATCH).serialize(this, context);
    }
  }

  /** Ingredient that matches any entity from a tag */
  @RequiredArgsConstructor
  private static class TagMatch extends EntityIngredient {
    private final TagKey<EntityType<?>> tag;
    private Set<EntityType<?>> types;

    @Override
    public boolean test(EntityType<?> type) {
      return type.is(tag);
    }

    @Override
    public Set<EntityType<?>> getTypes() {
      if (types == null) {
        types = RegistryHelper.getTagValueStream(BuiltInRegistries.ENTITY_TYPE, tag).collect(ImmutableSet.toImmutableSet());
      }
      return types;
    }

    @Override
    protected JsonElement serialize(TypedMap context) {
      return TAG_MATCH.serialize(this, context);
    }
  }

  /** Ingredient combining multiple */
  @RequiredArgsConstructor
  private static class Compound extends EntityIngredient {
    private final List<EntityIngredient> ingredients;
    private Set<EntityType<?>> allTypes;

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
    public Set<EntityType<?>> getTypes() {
      if (allTypes == null) {
        allTypes = ingredients.stream()
                              .flatMap(ingredient -> ingredient.getTypes().stream())
                              .collect(ImmutableSet.toImmutableSet());
      }
      return allTypes;
    }

    @Override
    protected JsonElement serialize(TypedMap context) {
      return COMPOUND.serialize(this, context);
    }
  }

  /** Simple wrapper around entity type for usage in JEI */
  public record EntityInput(EntityType<?> type) {
    public static final Codec<EntityInput> CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec().xmap(EntityInput::new, EntityInput::type);

    /** Wraps the given list into a list of entity inputs */
    public static List<EntityInput> wrap(Collection<EntityType<?>> types) {
      return types.stream().map(EntityInput::new).toList();
    }
  }
}

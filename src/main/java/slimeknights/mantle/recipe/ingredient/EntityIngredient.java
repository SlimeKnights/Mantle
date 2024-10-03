package slimeknights.mantle.recipe.ingredient;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeSpawnEggItem;
import slimeknights.mantle.data.loadable.IAmLoadable;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.mapping.EitherLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.RegistryHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Ingredient accepting an entity or an entity tag as an input
 */
public abstract class EntityIngredient implements Predicate<EntityType<?>>, IAmLoadable {
  /** Empty entity ingredient, matching nothing. This ingredient does not parse from JSON, use defaulting methods if you wish to use it */
  public static final EntityIngredient EMPTY = new Compound(Collections.emptyList());


  /* Loadables */

  /** Creates a builder with set and tag */
  private static EitherLoadable.TypedBuilder<EntityIngredient> loadableBuilder() {
    return EitherLoadable.<EntityIngredient>typed().key("types", SET_MATCH).key("type", ENTRY_MATCH).key("tag", TAG_MATCH);
  }
  /** Loadable for a single value, notably not used for networking (though that probably would still work fine due to how EitherLoadable works) */
  private static final RecordLoadable<EntityIngredient> ENTRY_MATCH = RecordLoadable.create(Loadables.ENTITY_TYPE.requiredField("type", i -> {
    Set<EntityType<?>> types = i.getTypes();
    if (types.size() == 1) {
      return types.iterator().next();
    }
    throw new IllegalStateException("Cannot use entry match to serialize more than 1 entity");
  }), EntityIngredient::of);
  /** Loadable for a set match */
  private static final RecordLoadable<EntityIngredient> SET_MATCH = RecordLoadable.create(Loadables.ENTITY_TYPE.set().requiredField("types", EntityIngredient::getTypes), EntityIngredient::of);
  /** Loadable for a tag match */
  private static final RecordLoadable<TagMatch> TAG_MATCH = RecordLoadable.create(Loadables.ENTITY_TYPE_TAG.requiredField("tag", t -> t.tag), TagMatch::new);
  /** Loadable disallows nested lists, just handles nested tags and sets */
  private static final Loadable<Compound> COMPOUND = EntityIngredient.loadableBuilder().build(SET_MATCH).list(2).flatXmap(Compound::new, c -> c.ingredients);
  /** Loadable for any fluid ingredient */
  public static final Loadable<EntityIngredient> LOADABLE = loadableBuilder().array(COMPOUND).build(SET_MATCH);

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
      return ingredients.get(0);
    }
    return new Compound(ingredients);
  }


  /* Common methods */

  /**
   * Gets a list of entity types matched by this ingredient
   * @return  List of types
   */
  public abstract Set<EntityType<?>> getTypes();

  /**
   * Serializes this ingredient to JSON
   * @return  Json element of this ingredient
   */
  public JsonElement serialize() {
    return LOADABLE.serialize(this);
  }


  /** Writes this ingredient to the packet buffer */
  public void write(FriendlyByteBuf buffer) {
    SET_MATCH.encode(buffer, this);
  }

  /**
   * Reads an ingredient from the packet buffer
   * @param buffer  Buffer instance
   * @return  Ingredient instance
   */
  public static EntityIngredient read(FriendlyByteBuf buffer) {
    return SET_MATCH.decode(buffer);
  }


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

  /** Gets the list of eggs matching this ingredient, used for focus links in JEI */
  public List<ItemStack> getEggs() {
    if (eggs == null) {
      // use getDisplay to guarantee order is the same, just in case
      eggs = getDisplay().stream().map(type -> new ItemStack(Objects.requireNonNullElse(ForgeSpawnEggItem.fromEntityType(type.type), Items.AIR))).toList();
    }
    return eggs;
  }



  /* Impls */

  /** Ingredient that matches any entity from a set */
  @RequiredArgsConstructor
  private static class SetMatch extends EntityIngredient {
    private final Set<EntityType<?>> types;

    @Override
    public Loadable<?> loadable() {
      return types.size() == 1 ? ENTRY_MATCH : SET_MATCH;
    }

    @Override
    public boolean test(EntityType<?> type) {
      return types.contains(type);
    }

    @Override
    public Set<EntityType<?>> getTypes() {
      return types;
    }
  }

  /** Ingredient that matches any entity from a tag */
  @RequiredArgsConstructor
  private static class TagMatch extends EntityIngredient {
    private final TagKey<EntityType<?>> tag;
    private Set<EntityType<?>> types;

    @Override
    public Loadable<?> loadable() {
      return TAG_MATCH;
    }

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
  }

  /** Ingredient combining multiple */
  @RequiredArgsConstructor
  private static class Compound extends EntityIngredient {
    private final List<EntityIngredient> ingredients;
    private Set<EntityType<?>> allTypes;

    @Override
    public Loadable<?> loadable() {
      return COMPOUND;
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
    public Set<EntityType<?>> getTypes() {
      if (allTypes == null) {
        allTypes = ingredients.stream()
                              .flatMap(ingredient -> ingredient.getTypes().stream())
                              .collect(ImmutableSet.toImmutableSet());
      }
      return allTypes;
    }
  }

  /** Simple wrapper around entity type for usage in JEI */
  public record EntityInput(EntityType<?> type) {
    /** Wraps the given list into a list of entity inputs */
    public static List<EntityInput> wrap(Collection<EntityType<?>> types) {
      return types.stream().map(EntityInput::new).toList();
    }
  }
}

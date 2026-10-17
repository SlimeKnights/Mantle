package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.Codec;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.codec.MantleCodecs;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.common.CodecLoadable;
import slimeknights.mantle.network.codec.MantleStreamCodecs;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Ingredient accepting an entity or an entity tag as an input
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "of")
public class EntityIngredient implements Predicate<EntityType<?>> {
  /** Empty entity ingredient, matching nothing. Generally best to use defaulting methods if you wish to use this. */
  public static final EntityIngredient EMPTY = of(HolderSet.empty());

  /* Serializing */
  public static final Codec<EntityIngredient> CODEC = MantleCodecs.ENTITY_SET.xmap(EntityIngredient::new, i -> i.entities);
  public static final StreamCodec<RegistryFriendlyByteBuf,EntityIngredient> STREAM_CODEC = MantleStreamCodecs.ENTITY_SET.map(EntityIngredient::new, i -> i.entities);
  public static final Loadable<EntityIngredient> LOADABLE = new CodecLoadable.Registry<>(CODEC, STREAM_CODEC);

  /* Fields */
  /** Set of entities to match */
  private final HolderSet<EntityType<?>> entities;
  /** List of entity types to display outside of JEI. */
  private List<EntityType<?>> types;
  /** List of {@link EntityInput} to display in JEI. */
  private List<EntityInput> inputs;
  /** List of spawn egg items for JEI focus links */
  private List<@Nullable ItemStack> eggs;


  /* Constructors */

  /**
   * Creates an ingredient to match a set of types
   */
  @SuppressWarnings("deprecation")
  public static EntityIngredient of(List<EntityType<?>> list) {
    if (list.isEmpty()) {
      return EMPTY;
    }
    return of(HolderSet.direct(list.stream().map(EntityType::builtInRegistryHolder).toList()));
  }

  /** Creates an ingredient to match a set of types */
  public static EntityIngredient of(EntityType<?> ... types) {
    return of(List.of(types));
  }

  /** Creates an ingredient to match a tags */
  public static EntityIngredient of(TagKey<EntityType<?>> tag) {
    return of(IngredientHelper.tagSet(BuiltInRegistries.ENTITY_TYPE, tag));
  }


  /* Common methods */

  /** Tests if the given entity type matches this ingredient */
  @Override
  public boolean test(EntityType<?> entityType) {
    return entityType.is(entities);
  }

  /** Tests if the given entity matches this ingredient */
  public boolean test(Entity entity) {
    return test(entity.getType());
  }


  /* Display */

  /**
   * Gets a list of entity types matched by this ingredient
   * @return  List of types
   */
  public List<EntityType<?>> getTypes() {
    if (types == null) {
      types = entities.stream().map(Holder::value).collect(Collectors.toUnmodifiableList());
    }
    return types;
  }

  /** Gets the list of eggs matching this ingredient, used for display in JEI as it cannot do entity type */
  public List<EntityInput> getDisplay() {
    if (inputs == null) {
      inputs = EntityInput.wrap(getTypes());
    }
    return inputs;
  }

  /**
   * Gets the list of eggs matching this ingredient, used for focus links in JEI.
   * Note the list may contain null for any entities whose egg does not exist.
   */
  public List<@Nullable ItemStack> getEggs() {
    if (eggs == null) {
      // use getDisplay to guarantee order is the same, just in case
      eggs = getTypes().stream().map(type -> {
        Item item = SpawnEggItem.byId(type);
        if (item == null) {
          return null;
        }
        return new ItemStack(item);
      }).toList();
    }
    return eggs;
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

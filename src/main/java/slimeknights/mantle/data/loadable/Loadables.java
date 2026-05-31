package slimeknights.mantle.data.loadable;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.data.MantleCodecs;
import slimeknights.mantle.data.loadable.common.CodecLoadable;
import slimeknights.mantle.data.loadable.common.DynamicRegistryLoadable;
import slimeknights.mantle.data.loadable.common.LazyRegistryLoadable;
import slimeknights.mantle.data.loadable.common.RegistryLoadable;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable.IntNetwork;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;

import java.util.function.BiFunction;

/** Various loadable instances provided by this mod */
@SuppressWarnings({"deprecation", "unused"})
public class Loadables {
  private Loadables() {}

  /** Alias for the resource location loadable as it's a common need */
  public static final StringLoadable<ResourceLocation> RESOURCE_LOCATION = ResourceLocationLoadable.DEFAULT;
  public static final StringLoadable<ItemAbility> TOOL_ACTION = StringLoadable.DEFAULT.flatXmap(ItemAbility::get, ItemAbility::name);

  /* Registries */
  public static final ResourceLocationLoadable<SoundEvent> SOUND_EVENT = new RegistryLoadable<>(BuiltInRegistries.SOUND_EVENT);
  public static final ResourceLocationLoadable<Fluid> FLUID = new RegistryLoadable<>(BuiltInRegistries.FLUID);
  public static final ResourceLocationLoadable<FluidType> FLUID_TYPE = new LazyRegistryLoadable<>(NeoForgeRegistries.Keys.FLUID_TYPES);
  public static final ResourceLocationLoadable<MobEffect> MOB_EFFECT = new RegistryLoadable<>(BuiltInRegistries.MOB_EFFECT);
  public static final ResourceLocationLoadable<Block> BLOCK = new RegistryLoadable<>(BuiltInRegistries.BLOCK);
  public static final ResourceLocationLoadable<Enchantment> ENCHANTMENT = new DynamicRegistryLoadable<>(Registries.ENCHANTMENT);
  public static final ResourceLocationLoadable<EntityType<?>> ENTITY_TYPE = new RegistryLoadable<>(BuiltInRegistries.ENTITY_TYPE);
  public static final ResourceLocationLoadable<Item> ITEM = new RegistryLoadable<>(BuiltInRegistries.ITEM);
  public static final ResourceLocationLoadable<Potion> POTION = new RegistryLoadable<>(BuiltInRegistries.POTION);
  public static final ResourceLocationLoadable<ParticleType<?>> PARTICLE_TYPE = new RegistryLoadable<>(BuiltInRegistries.PARTICLE_TYPE);
  public static final ResourceLocationLoadable<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new RegistryLoadable<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
  public static final ResourceLocationLoadable<Attribute> ATTRIBUTE = new RegistryLoadable<>(BuiltInRegistries.ATTRIBUTE);
  public static final ResourceLocationLoadable<RecipeType<?>> RECIPE_TYPE = new RegistryLoadable<>(BuiltInRegistries.RECIPE_TYPE);

  /* Non-default registries */
  public static final StringLoadable<Fluid> NON_EMPTY_FLUID = notValue(FLUID, Fluids.EMPTY, "Fluid cannot be empty");
  public static final StringLoadable<Block> NON_EMPTY_BLOCK = notValue(BLOCK, Blocks.AIR, "Block cannot be air");
  public static final StringLoadable<Item> NON_EMPTY_ITEM = notValue(ITEM, Items.AIR, "Item cannot be empty");

  /* Tag keys */
  public static final StringLoadable<TagKey<Fluid>> FLUID_TAG = tagKey(Registries.FLUID);
  public static final StringLoadable<TagKey<MobEffect>> MOB_EFFECT_TAG = tagKey(Registries.MOB_EFFECT);
  public static final StringLoadable<TagKey<Block>> BLOCK_TAG = tagKey(Registries.BLOCK);
  public static final StringLoadable<TagKey<Enchantment>> ENCHANTMENT_TAG = tagKey(Registries.ENCHANTMENT);
  public static final StringLoadable<TagKey<EntityType<?>>> ENTITY_TYPE_TAG = tagKey(Registries.ENTITY_TYPE);
  public static final StringLoadable<TagKey<Item>> ITEM_TAG = tagKey(Registries.ITEM);
  public static final StringLoadable<TagKey<Potion>> POTION_TAG = tagKey(Registries.POTION);
  public static final StringLoadable<TagKey<BlockEntityType<?>>> BLOCK_ENTITY_TYPE_TAG = tagKey(Registries.BLOCK_ENTITY_TYPE);
  public static final StringLoadable<TagKey<DamageType>> DAMAGE_TYPE_TAG = tagKey(Registries.DAMAGE_TYPE);

  /* Resource keys */
  public static final StringLoadable<ResourceKey<DamageType>> DAMAGE_TYPE_KEY = resourceKey(Registries.DAMAGE_TYPE);
  public static final StringLoadable<ResourceKey<Enchantment>> ENCHANTMENT_KEY = resourceKey(Registries.ENCHANTMENT);

  /* Loot tables */
  /** Loadable for a loot entry instance */
  public static final Loadable<LootPoolEntryContainer> LOOT_ENTRY = new CodecLoadable<>(MantleCodecs.LOOT_ENTRY);

  /** Loadable for a rotation value, from 0 to 270 */
  public static final Loadable<Integer> ROTATION = new IntLoadable(0, 270, IntNetwork.SHORT).validate((value, error) -> {
    if (!ModelHelper.checkRotation(value)) {
      throw error.create("Rotation must be 0/90/180/270");
    }
    return value;
  });

  /** Loadable for vanilla direction values */
  public static final EnumLoadable<Direction> DIRECTION = new EnumLoadable<>(Direction.class);


  /* Helpers */

  /** Creates a tag key loadable */
  public static <T> StringLoadable<TagKey<T>> tagKey(ResourceKey<? extends Registry<T>> registry) {
    return RESOURCE_LOCATION.flatXmap(key -> TagKey.create(registry, key), TagKey::location);
  }

  /** Creates a resource key loadable */
  public static <T> StringLoadable<ResourceKey<T>> resourceKey(ResourceKey<? extends Registry<T>> registry) {
    return RESOURCE_LOCATION.flatXmap(key -> ResourceKey.create(registry, key), ResourceKey::location);
  }

  /** Maps a loadable to a variant that disallows a particular value */
  public static <T> StringLoadable<T> notValue(StringLoadable<T> loadable, T notValue, String errorMsg) {
    BiFunction<T,ErrorFactory,T> mapper = (value, error) -> {
      if (value == notValue) {
        throw error.create(errorMsg);
      }
      return value;
    };
    return loadable.xmap(mapper, mapper);
  }
}

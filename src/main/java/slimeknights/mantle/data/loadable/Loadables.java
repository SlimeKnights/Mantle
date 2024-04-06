package slimeknights.mantle.data.loadable;

import net.minecraft.ResourceLocationException;
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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ToolAction;
import slimeknights.mantle.data.loadable.common.RegistryLoadable;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;

import java.util.function.BiFunction;

/** Various loadable instances provided by this mod */
@SuppressWarnings({"deprecation", "unused"})
public class Loadables {
  private Loadables() {}

  /** Loadable for a resource location */
  public static final StringLoadable<ResourceLocation> RESOURCE_LOCATION = StringLoadable.DEFAULT.xmap((s, e) -> {
    try {
      return new ResourceLocation(s);
    } catch (ResourceLocationException ex) {
      throw e.create(ex);
    }
  }, (r, e) -> r.toString());
  public static final StringLoadable<ToolAction> TOOL_ACTION = StringLoadable.DEFAULT.flatXmap(ToolAction::get, ToolAction::name);

  /* Registries */
  public static final StringLoadable<SoundEvent> SOUND_EVENT = new RegistryLoadable<>(BuiltInRegistries.SOUND_EVENT);
  public static final StringLoadable<Fluid> FLUID = new RegistryLoadable<>(BuiltInRegistries.FLUID);
  public static final StringLoadable<MobEffect> MOB_EFFECT = new RegistryLoadable<>(BuiltInRegistries.MOB_EFFECT);
  public static final StringLoadable<Block> BLOCK = new RegistryLoadable<>(BuiltInRegistries.BLOCK);
  public static final StringLoadable<Enchantment> ENCHANTMENT = new RegistryLoadable<>(BuiltInRegistries.ENCHANTMENT);
  public static final StringLoadable<EntityType<?>> ENTITY_TYPE = new RegistryLoadable<>(BuiltInRegistries.ENTITY_TYPE);
  public static final StringLoadable<Item> ITEM = new RegistryLoadable<>(BuiltInRegistries.ITEM);
  public static final StringLoadable<Potion> POTION = new RegistryLoadable<>(BuiltInRegistries.POTION);
  public static final StringLoadable<ParticleType<?>> PARTICLE_TYPE = new RegistryLoadable<>(BuiltInRegistries.PARTICLE_TYPE);
  public static final StringLoadable<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new RegistryLoadable<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
  public static final StringLoadable<Attribute> ATTRIBUTE = new RegistryLoadable<>(BuiltInRegistries.ATTRIBUTE);

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


  /* Helpers */

  /** Creates a tag key loadable */
  public static <T> StringLoadable<TagKey<T>> tagKey(ResourceKey<? extends Registry<T>> registry) {
    return RESOURCE_LOCATION.flatXmap(key -> TagKey.create(registry, key), TagKey::location);
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

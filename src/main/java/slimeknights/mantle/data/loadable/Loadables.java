package slimeknights.mantle.data.loadable;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
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
  public static final Loadable<ResourceLocation> RESOURCE_LOCATION = StringLoadable.DEFAULT.map((s, e) -> {
    try {
      return new ResourceLocation(s);
    } catch (ResourceLocationException ex) {
      throw e.create(ex);
    }
  }, (r, e) -> r.toString());
  public static final Loadable<ToolAction> TOOL_ACTION = StringLoadable.DEFAULT.flatMap(ToolAction::get, ToolAction::name);

  /* Registries */
  public static final Loadable<SoundEvent> SOUND_EVENT = new RegistryLoadable<>(Registry.SOUND_EVENT);
  public static final Loadable<Fluid> FLUID = new RegistryLoadable<>(Registry.FLUID);
  public static final Loadable<MobEffect> MOB_EFFECT = new RegistryLoadable<>(Registry.MOB_EFFECT);
  public static final Loadable<Block> BLOCK = new RegistryLoadable<>(Registry.BLOCK);
  public static final Loadable<Enchantment> ENCHANTMENT = new RegistryLoadable<>(Registry.ENCHANTMENT);
  public static final Loadable<EntityType<?>> ENTITY_TYPE = new RegistryLoadable<>(Registry.ENTITY_TYPE);
  public static final Loadable<Item> ITEM = new RegistryLoadable<>(Registry.ITEM);
  public static final Loadable<Potion> POTION = new RegistryLoadable<>(Registry.POTION);
  public static final Loadable<ParticleType<?>> PARTICLE_TYPE = new RegistryLoadable<>(Registry.PARTICLE_TYPE);
  public static final Loadable<BlockEntityType<?>> BLOCK_ENTITY_TYPE = new RegistryLoadable<>(Registry.BLOCK_ENTITY_TYPE);
  public static final Loadable<Attribute> ATTRIBUTE = new RegistryLoadable<>(Registry.ATTRIBUTE);

  /* Non-default registries */
  public static final Loadable<Fluid> NON_EMPTY_FLUID = notValue(FLUID, Fluids.EMPTY, "Fluid cannot be empty");
  public static final Loadable<Block> NON_EMPTY_BLOCK = notValue(BLOCK, Blocks.AIR, "Block cannot be air");
  public static final Loadable<Item> NON_EMPTY_ITEM = notValue(ITEM, Items.AIR, "Item cannot be empty");


  /* Tag keys */
  public static final Loadable<TagKey<Fluid>> FLUID_TAG = tagKey(Registry.FLUID_REGISTRY);
  public static final Loadable<TagKey<MobEffect>> MOB_EFFECT_TAG = tagKey(Registry.MOB_EFFECT_REGISTRY);
  public static final Loadable<TagKey<Block>> BLOCK_TAG = tagKey(Registry.BLOCK_REGISTRY);
  public static final Loadable<TagKey<Enchantment>> ENCHANTMENT_TAG = tagKey(Registry.ENCHANTMENT_REGISTRY);
  public static final Loadable<TagKey<EntityType<?>>> ENTITY_TYPE_TAG = tagKey(Registry.ENTITY_TYPE_REGISTRY);
  public static final Loadable<TagKey<Item>> ITEM_TAG = tagKey(Registry.ITEM_REGISTRY);
  public static final Loadable<TagKey<Potion>> POTION_TAG = tagKey(Registry.POTION_REGISTRY);
  public static final Loadable<TagKey<BlockEntityType<?>>> BLOCK_ENTITY_TYPE_TAG = tagKey(Registry.BLOCK_ENTITY_TYPE_REGISTRY);


  /* Helpers */

  /** Creates a tag key loadable */
  public static <T> Loadable<TagKey<T>> tagKey(ResourceKey<? extends Registry<T>> registry) {
    return RESOURCE_LOCATION.flatMap(key -> TagKey.create(registry, key), TagKey::location);
  }

  /** Maps a loadable to a variant that disallows a particular value */
  public static <T> Loadable<T> notValue(Loadable<T> loadable, T notValue, String errorMsg) {
    BiFunction<T,ErrorFactory,T> mapper = (value, error) -> {
      if (value == notValue) {
        throw error.create(errorMsg);
      }
      return value;
    };
    return loadable.map(mapper, mapper);
  }
}

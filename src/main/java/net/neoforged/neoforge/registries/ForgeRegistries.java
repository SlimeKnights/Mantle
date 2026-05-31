package slimeknights.mantle.compat.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Compatibility shim for old ForgeRegistries constants. */
public final class ForgeRegistries {
  private ForgeRegistries() {}

  public static final ForgeRegistry<Block> BLOCKS = new ForgeRegistry<>(BuiltInRegistries.BLOCK);
  public static final ForgeRegistry<Item> ITEMS = new ForgeRegistry<>(BuiltInRegistries.ITEM);
  public static final ForgeRegistry<Fluid> FLUIDS = new ForgeRegistry<>(BuiltInRegistries.FLUID);
  public static final ForgeRegistry<MobEffect> MOB_EFFECTS = new ForgeRegistry<>(BuiltInRegistries.MOB_EFFECT);
  public static final ForgeRegistry<EntityType<?>> ENTITY_TYPES = new ForgeRegistry<>(BuiltInRegistries.ENTITY_TYPE);
  public static final ForgeRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new ForgeRegistry<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
  public static final ForgeRegistry<Potion> POTIONS = new ForgeRegistry<>(BuiltInRegistries.POTION);
  public static final ForgeRegistry<Enchantment> ENCHANTMENTS = new ForgeRegistry<>(null);
  public static final ForgeRegistry<SoundEvent> SOUND_EVENTS = new ForgeRegistry<>(BuiltInRegistries.SOUND_EVENT);
  public static final ResourceKey<Registry<Feature<?>>> FEATURES = Registries.FEATURE;

  public static final class Keys {
    public static final ResourceKey<Registry<BiomeModifier>> BIOME_MODIFIERS = NeoForgeRegistries.Keys.BIOME_MODIFIERS;
    public static final ResourceKey<Registry<EntityDataSerializer<?>>> ENTITY_DATA_SERIALIZERS = NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS;
    public static final ResourceKey<Registry<ItemDisplayContext>> DISPLAY_CONTEXTS = ResourceKey.createRegistryKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoforge", "display_contexts"));
  }
}

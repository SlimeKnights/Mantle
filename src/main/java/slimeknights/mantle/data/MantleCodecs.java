package slimeknights.mantle.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import slimeknights.mantle.util.RegistryHelper;

/** This class contains codecs for various vanilla things that we need to use in codecs. */
public class MantleCodecs {
  /** Codec for a registry key */
  public static final Codec<ResourceKey<? extends Registry<?>>> REGISTRY_KEY = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::registry);
  /** Codec for a registry key */
  public static final MapCodec<ResourceKey<? extends Registry<?>>> REGISTRY_FIELD = REGISTRY_KEY.optionalFieldOf("registry", Registries.ITEM);

  /** Codec for a tag key from any registry */
  public static final MapCodec<TagKey<?>> TAG_KEY = RecordCodecBuilder.mapCodec(instance -> instance.group(
    REGISTRY_FIELD.forGetter(TagKey::registry),
    ResourceLocation.CODEC.fieldOf("location").forGetter(TagKey::location)
    ).apply(instance, (registry, location) -> TagKey.create(RegistryHelper.castKey(registry), location)));
  /** Codec for a block tag */
  public static final Codec<TagKey<Block>> BLOCK_TAG = TagKey.codec(Registries.BLOCK);
}

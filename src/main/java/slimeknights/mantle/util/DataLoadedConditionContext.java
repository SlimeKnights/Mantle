package slimeknights.mantle.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.conditions.ICondition;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Condition context to use when data has already been loaded, used in books for processing their conditions for instance. */
public enum DataLoadedConditionContext implements ICondition.IContext {
  INSTANCE;

  @Override
  public <T> Collection<Holder<T>> getTag(TagKey<T> key) {
    Registry<T> registry = RegistryHelper.getRegistry(key.registry());
    if (registry != null) {
      Optional<HolderSet.Named<T>> tag = registry.getTag(key);
      if (tag.isPresent()) {
        return tag.get().contents;
      }
    }
    return Set.of();
  }

  @Override
  public <T> Map<ResourceLocation,Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> key) {
    Registry<T> registry = RegistryHelper.getRegistry(key);
    if (registry != null) {
      return registry.getTags().collect(Collectors.toMap(entry -> entry.getFirst().location(), entry -> entry.getSecond().contents));
    }
    return Map.of();
  }
}

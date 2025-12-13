package slimeknights.mantle.util;

import net.minecraft.resources.ResourceLocation;

/** @deprecated use new utilities from {@link ResourceLocation} */
@Deprecated(forRemoval = true)
public interface IdExtender<T extends ResourceLocation> {
  /** Extender for standard resource locations */
  LocationExtender INSTANCE = new LocationExtender() {};

  /** Creates a resource location */
  T location(String namespace, String path);

  /** @deprecated use {@link JsonHelper#wrap(ResourceLocation, String, String)} */
  @Deprecated(forRemoval = true)
  default T wrap(ResourceLocation location, String prefix, String suffix) {
    return location(location.getNamespace(), prefix + location.getPath() + suffix);
  }

  /** @deprecated use {@link ResourceLocation#withPrefix(String)} */
  @Deprecated(forRemoval = true)
  default T prefix(ResourceLocation location, String prefix) {
    return location(location.getNamespace(), prefix + location.getPath());
  }

  /** @deprecated use {@link ResourceLocation#withSuffix(String)} */
  @Deprecated(forRemoval = true)
  default T suffix(ResourceLocation location, String suffix) {
    return location(location.getNamespace(), location.getPath() + suffix);
  }

  /** Extender for specifically resource locations, used in recipe helpers */
  interface LocationExtender extends IdExtender<ResourceLocation> {
    @Override
    default ResourceLocation location(String namespace, String path) {
      return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
  }
}

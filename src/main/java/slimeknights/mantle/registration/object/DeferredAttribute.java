package slimeknights.mantle.registration.object;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.registries.DeferredHolder;

/** {@link DeferredHolder} for an attribute. Mostly simplifies the generics. */
public class DeferredAttribute<T extends Attribute> extends DeferredHolder<Attribute,T> {
  protected DeferredAttribute(ResourceKey<Attribute> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends Attribute> DeferredAttribute<T> createAttribute(ResourceLocation key) {
    return createAttribute(ResourceKey.create(Registries.ATTRIBUTE, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends Attribute> DeferredAttribute<T> createAttribute(ResourceKey<Attribute> key) {
    return new DeferredAttribute<T>(key);
  }
}

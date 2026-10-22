package slimeknights.mantle.registration.holder;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

/** {@link DeferredHolder} for an {@link ArgumentTypeInfo}. Mostly simplifies the generics. */
public class DeferredArgument<T extends ArgumentTypeInfo<?,?>> extends DeferredHolder<ArgumentTypeInfo<?,?>,T> {
  protected DeferredArgument(ResourceKey<ArgumentTypeInfo<?,?>> key) {
    super(key);
  }

  /** Creates a new instance for the given resource location */
  public static <T extends ArgumentTypeInfo<?,?>> DeferredArgument<T> createArgument(ResourceLocation key) {
    return createArgument(ResourceKey.create(Registries.COMMAND_ARGUMENT_TYPE, key));
  }

  /** Creates a new instance for the given resource key */
  public static <T extends ArgumentTypeInfo<?,?>> DeferredArgument<T> createArgument(ResourceKey<ArgumentTypeInfo<?,?>> key) {
    return new DeferredArgument<>(key);
  }
}

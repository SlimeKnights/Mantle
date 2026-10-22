package slimeknights.mantle.registration.deferred;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeknights.mantle.registration.RegistrationHelper;
import slimeknights.mantle.registration.holder.DeferredArgument;

import java.util.function.Function;
import java.util.function.Supplier;

/** Register for argument types that automatically handles registering with {@link ArgumentTypeInfos#registerByClass(Class, ArgumentTypeInfo)} */
@SuppressWarnings("unused")  // API
public class ArgumentTypeDeferredRegister extends DeferredRegister<ArgumentTypeInfo<?,?>> {
  public ArgumentTypeDeferredRegister(String modID) {
    super(Registries.COMMAND_ARGUMENT_TYPE, modID);
  }

  @Override
  protected <I extends ArgumentTypeInfo<?, ?>> DeferredArgument<I> createHolder(ResourceKey<? extends Registry<ArgumentTypeInfo<?, ?>>> registryKey, ResourceLocation key) {
    return DeferredArgument.createArgument(ResourceKey.create(registryKey, key));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <I extends ArgumentTypeInfo<?, ?>> DeferredArgument<I> register(String name, Function<ResourceLocation, ? extends I> func) {
    return (DeferredArgument<I>) super.register(name, func);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <I extends ArgumentTypeInfo<?, ?>> DeferredArgument<I> register(String name, Supplier<? extends I> sup) {
    return (DeferredArgument<I>) super.register(name, sup);
  }

  /**
   * Registers an argument type
   * @param name           Name of the argument
   * @param argumentClass  Class of the argument
   * @param supplier       Supplier to the argument info
   * @param <A>  Argument type
   * @param <T>  Argument info template type
   * @param <I>  Argument info type
   * @return  Registry object
   */
  public <A extends ArgumentType<?>,T extends ArgumentTypeInfo.Template<A>,I extends ArgumentTypeInfo<A,T>> DeferredArgument<I> register(String name, Class<? super A> argumentClass, Supplier<I> supplier) {
    return register(name, () -> {
      I info = supplier.get();
      ArgumentTypeInfos.registerByClass(RegistrationHelper.genericArgumentType(argumentClass), info);
      return info;
    });
  }

  /**
   * Registers a context free singleton argument
   * @param name           Name of the argument
   * @param argumentClass  Class of the argument
   * @param supplier       Supplier to the argument default
   * @param <A>  Argument type
   * @return  Registry object
   */
  public <A extends ArgumentType<?>> DeferredArgument<SingletonArgumentInfo<A>> registerSingleton(String name, Class<A> argumentClass, Supplier<A> supplier) {
    return register(name, argumentClass, () -> SingletonArgumentInfo.contextFree(supplier));
  }
}

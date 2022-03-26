package slimeknights.mantle.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Argument type that supports any vanilla registry. Due to the lack of context, not a true argument type but rather helpers
 */
public class RegistryArgument {
  /* Name is invalid */
  private static final DynamicCommandExceptionType NOT_FOUND = new DynamicCommandExceptionType(name -> new TranslatableComponent("command.mantle.registry.not_found", name));

  /** Creates an argument instance */
  public static ArgumentType<ResourceLocation> registry() {
    return ResourceLocationArgument.id();
  }

  /** Gets the result of this argument */
  public static Registry<?> getResult(CommandContext<? extends SharedSuggestionProvider> pContext, String pName) throws CommandSyntaxException {
    ResourceLocation name = pContext.getArgument(pName, ResourceLocation.class);
    return pContext.getSource().registryAccess()
                   .registry(ResourceKey.createRegistryKey(name))
                   .orElseThrow(() -> NOT_FOUND.create(name));
  }
}

package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.IForgeRegistry;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.mapping.EnumMapLoadable;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Map;

/** Special loadable for display contexts due to the Forge weirdness in {@link ItemDisplayContext} */
public enum DisplayContextLoadable implements ResourceLocationLoadable<ItemDisplayContext> {
  INSTANCE;

  @Override
  public ItemDisplayContext fromKey(ResourceLocation name, String key, TypedMap context) {
    IForgeRegistry<ItemDisplayContext> registry = ForgeRegistries.DISPLAY_CONTEXTS.get();
    if (registry.containsKey(name)) {
      ItemDisplayContext value = registry.getValue(name);
      if (value != null) {
        return value;
      }
    }
    throw new JsonSyntaxException("Unable to parse " + key + " as the ItemDisplayContext registry does not contain ID " + name);
  }

  @Override
  public ResourceLocation getKey(ItemDisplayContext object) {
    IForgeRegistry<ItemDisplayContext> registry = ForgeRegistries.DISPLAY_CONTEXTS.get();
    ResourceLocation location = registry.getKey(object);
    if (location == null) {
      throw new RuntimeException("ItemDisplayContext registry does not contain object " + object);
    }
    return location;
  }

  @Override
  public ItemDisplayContext decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    return buffer.readRegistryIdUnsafe(ForgeRegistries.DISPLAY_CONTEXTS.get());
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, ItemDisplayContext value) {
    buffer.writeRegistryIdUnsafe(ForgeRegistries.DISPLAY_CONTEXTS.get(), value);
  }

  @Override
  public <V> Loadable<Map<ItemDisplayContext,V>> mapWithValues(Loadable<V> valueLoadable, int minSize) {
    return new EnumMapLoadable<>(ItemDisplayContext.class, this, valueLoadable, minSize);
  }
}

package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.mapping.EnumMapLoadable;
import slimeknights.mantle.data.loadable.primitive.ResourceLocationLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Map;

/** Special loadable for display contexts due to the Forge weirdness in {@link ItemDisplayContext} */
public enum DisplayContextLoadable implements ResourceLocationLoadable<ItemDisplayContext> {
  INSTANCE;

  private Registry<ItemDisplayContext> getRegistry() {
    return NeoForgeRegistries.ITEM_DISPLAY_CONTEXTS;
  }

  @Override
  public ItemDisplayContext fromKey(ResourceLocation name, String key, TypedMap context) {
    Registry<ItemDisplayContext> registry = getRegistry();
    return registry.getOptional(name).orElseThrow(() -> 
      new JsonSyntaxException("Unable to parse " + key + " as the ItemDisplayContext registry does not contain ID " + name));
  }

  @Override
  public ResourceLocation getKey(ItemDisplayContext object) {
    Registry<ItemDisplayContext> registry = getRegistry();
    ResourceLocation location = registry.getKey(object);
    if (location == null) {
      throw new RuntimeException("ItemDisplayContext registry does not contain object " + object);
    }
    return location;
  }

  @Override
  public ItemDisplayContext decode(FriendlyByteBuf buffer, TypedMap context) {
    return buffer.readById(getRegistry());
  }

  @Override
  public void encode(FriendlyByteBuf buffer, ItemDisplayContext value) {
    buffer.writeId(getRegistry(), value);
  }

  @Override
  public <V> Loadable<Map<ItemDisplayContext,V>> mapWithValues(Loadable<V> valueLoadable, int minSize) {
    return new EnumMapLoadable<>(ItemDisplayContext.class, this, valueLoadable, minSize);
  }
}

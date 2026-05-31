package slimeknights.mantle.compat.neoforged.neoforge.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.lang.reflect.Method;

public interface IIngredientSerializer<T> {
  T parse(JsonObject json);

  T parse(FriendlyByteBuf buffer);

  void write(FriendlyByteBuf buffer, T ingredient);

  default JsonElement serialize(T ingredient) {
    try {
      Method method = ingredient.getClass().getMethod("toJson");
      return (JsonElement)method.invoke(ingredient);
    } catch (ReflectiveOperationException e) {
      throw new UnsupportedOperationException("Ingredient serializer cannot serialize " + ingredient.getClass().getName(), e);
    }
  }

  default MapCodec<T> codec() {
    return MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH.xmap(dynamic -> {
      JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
      return parse(json.getAsJsonObject());
    }, ingredient -> new Dynamic<>(JsonOps.INSTANCE, serialize(ingredient))));
  }

  default StreamCodec<RegistryFriendlyByteBuf,T> streamCodec() {
    return StreamCodec.of(this::write, this::parse);
  }
}

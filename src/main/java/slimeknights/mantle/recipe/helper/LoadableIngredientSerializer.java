package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/** Ingredient serializer made using loadables */
public record LoadableIngredientSerializer<T>(RecordLoadable<T> loadable) {
  public T parse(FriendlyByteBuf buffer) {
    return loadable.decode(buffer);
  }

  public T parse(JsonObject json) {
    return loadable.deserialize(json);
  }

  public void write(FriendlyByteBuf buffer, T ingredient) {
    loadable.encode(buffer, ingredient);
  }

  /** Serializes the ingredient to JSON */
  public JsonObject serialize(T ingredient) {
    JsonObject json = new JsonObject();
    loadable.serialize(ingredient, json);
    return json;
  }

  /** Creates a map codec for NeoForge custom ingredient registration. */
  public MapCodec<T> codec() {
    return MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH.xmap(dynamic -> {
      JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();
      return parse(json.getAsJsonObject());
    }, ingredient -> new Dynamic<>(JsonOps.INSTANCE, serialize(ingredient))));
  }

  /** Creates a stream codec for NeoForge custom ingredient network syncing. */
  public StreamCodec<RegistryFriendlyByteBuf,T> streamCodec() {
    return StreamCodec.of(this::write, this::parse);
  }
}

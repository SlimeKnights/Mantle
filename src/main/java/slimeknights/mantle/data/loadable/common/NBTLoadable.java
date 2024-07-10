package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.function.Function;

/** Loadable for reading NBT, converting from a JSON object to a tag.*/
public enum NBTLoadable implements RecordLoadable<CompoundTag> {
  /** Disallows reading NBT from a string in the Forge style*/
  DISALLOW_STRING,
  /** Allows reading NBT from a string in the forge style */
  ALLOW_STRING;

  @Override
  public CompoundTag deserialize(JsonObject json, TypedMap context) {
    return (CompoundTag)JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
  }

  @Override
  public CompoundTag convert(JsonElement element, String key) {
    if (this == ALLOW_STRING && !element.isJsonObject()) {
      try {
        return TagParser.parseTag(JsonHelper.DEFAULT_GSON.toJson(element));
      } catch (CommandSyntaxException e) {
        throw new JsonSyntaxException("Invalid NBT Entry: ", e);
      }
    }
    return RecordLoadable.super.convert(element, key);
  }

  @Override
  public JsonObject serialize(CompoundTag object) {
    return NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, object).getAsJsonObject();
  }

  @Override
  public void serialize(CompoundTag object, JsonObject json) {
    json.entrySet().addAll(serialize(object).entrySet());
  }

  @Override
  public CompoundTag decode(FriendlyByteBuf buffer, TypedMap context) {
    CompoundTag tag = buffer.readNbt();
    if (tag == null) {
      return new CompoundTag();
    }
    return tag;
  }

  @Override
  public void encode(FriendlyByteBuf buffer, CompoundTag object) {
    buffer.writeNbt(object);
  }

  @Override
  public <P> LoadableField<CompoundTag,P> nullableField(String key, Function<P,CompoundTag> getter) {
    return new NullableNBTField<>(this, key, getter);
  }


  /** Special implementation of nullable field to compact the buffer since it natively handles nullable NBT */
  private record NullableNBTField<P>(Loadable<CompoundTag> loadable, String key, Function<P,CompoundTag> getter) implements LoadableField<CompoundTag,P> {
    @Nullable
    @Override
    public CompoundTag get(JsonObject json) {
      return loadable.getOrDefault(json, key, null);
    }

    @Override
    public void serialize(P parent, JsonObject json) {
      CompoundTag nbt = getter.apply(parent);
      if (nbt != null) {
        json.add(key, loadable.serialize(nbt));
      }
    }

    @Nullable
    @Override
    public CompoundTag decode(FriendlyByteBuf buffer) {
      return buffer.readNbt();
    }

    @Override
    public void encode(FriendlyByteBuf buffer, P parent) {
      buffer.writeNbt(getter.apply(parent));
    }
  }
}

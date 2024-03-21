package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.JsonHelper;

/** Loadable for reading NBT, converting from a JSON object to a tag.*/
public enum NBTLoadable implements RecordLoadable<CompoundTag> {
  /** Disallows reading NBT from a string in the Forge style*/
  DISALLOW_STRING,
  /** Allows reading NBT from a string in the forge style */
  ALLOW_STRING;

  @Override
  public CompoundTag deserialize(JsonObject json) {
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
  public CompoundTag fromNetwork(FriendlyByteBuf buffer) throws DecoderException {
    CompoundTag tag = buffer.readNbt();
    if (tag == null) {
      return new CompoundTag();
    }
    return tag;
  }

  @Override
  public void toNetwork(CompoundTag object, FriendlyByteBuf buffer) throws EncoderException {
    buffer.writeNbt(object);
  }
}

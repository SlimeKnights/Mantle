package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

/**
 * Loadable containing one of two values based on the keys present. Designed for use in {@link #directField(Function)}
 * @param leftKey       If this key is present, the left loadable will be used
 * @param left          Left loadable
 * @param rightKey      If this key is present, the right loadable will be used
 * @param right         Right loadable
 * @param defaultValue  If neither key is present, this value will be returned. If null, will error instead.
 */
public record EitherRecordLoadable<L,R>(String leftKey, RecordLoadable<L> left, String rightKey, RecordLoadable<R> right, @Nullable Either<L,R> defaultValue) implements RecordLoadable<Either<L,R>> {
  public EitherRecordLoadable(String leftKey, Loadable<L> left, String rightKey, Loadable<R> right, @Nullable Either<L,R> defaultValue) {
    this(leftKey, new SimpleRecordLoadable<>(left, leftKey), rightKey, new SimpleRecordLoadable<>(right, rightKey), defaultValue);
  }

  public EitherRecordLoadable(String leftKey, Loadable<L> left, String rightKey, Loadable<R> right) {
    this(leftKey, left, rightKey, right, null);
  }

  @Override
  public Either<L, R> deserialize(JsonObject json, TypedMap context) {
    if (json.has(leftKey)) {
      return Either.left(left.deserialize(json, context));
    }
    if (json.has(rightKey)) {
      return Either.right(right.deserialize(json, context));
    }
    if (defaultValue != null) {
      return defaultValue;
    }
    throw new JsonSyntaxException("Json must contain either " + leftKey + " or " + rightKey);
  }

  @Override
  public void serializeInto(Either<L, R> either, JsonObject json, TypedMap context) {
    if (!either.equals(defaultValue)) {
      either.ifLeft(leftValue -> left.serializeInto(leftValue, json, context))
        .ifRight(rightValue -> right.serializeInto(rightValue, json, context));
    }
  }

  @Override
  public Either<L, R> decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    boolean isLeft = buffer.readBoolean();
    return isLeft ? Either.left(left.decode(buffer, context)) : Either.right(right.decode(buffer, context));
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, Either<L, R> either, TypedMap context) {
    Optional<L> leftOptional = either.left();
    if (leftOptional.isPresent()) {
      buffer.writeBoolean(true);
      left.encode(buffer, leftOptional.get(), context);
    } else {
      buffer.writeBoolean(false);
      right.encode(buffer, either.right().orElseThrow(), context);
    }
  }
}

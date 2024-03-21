package slimeknights.mantle.data.loadable.mapping;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.function.BiFunction;

/** Represents a trivially mapped loadable that serializes/writes to network like another loadable */
public record MappedLoadable<F,T>(Loadable<F> base, BiFunction<F,ErrorFactory,T> from, BiFunction<T,ErrorFactory,F> to) implements Loadable<T> {
  @Override
  public T convert(JsonElement element, String key) throws JsonSyntaxException {
    return from.apply(base.convert(element, key), ErrorFactory.JSON_SYNTAX_ERROR);
  }

  @Override
  public JsonElement serialize(T object) {
    return base.serialize(to.apply(object, ErrorFactory.RUNTIME));
  }

  @Override
  public T fromNetwork(FriendlyByteBuf buffer) {
    return from.apply(base.fromNetwork(buffer), ErrorFactory.DECODER_EXCEPTION);
  }

  @Override
  public void toNetwork(T object, FriendlyByteBuf buffer) {
    base.toNetwork(to.apply(object, ErrorFactory.ENCODER_EXCEPTION), buffer);
  }
}

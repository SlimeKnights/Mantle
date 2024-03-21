package slimeknights.mantle.data.loadable.field;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/**
 * Interface for fields in a {@link RecordLoadable}
 * @param <P>  Parent object
 * @param <T>  Loadable type
 */
public interface LoadableField<T,P> {
  /**
   * Gets the loadable from the given JSON
   * @param json  JSON object
   * @return  Parsed loadable value
   */
  T get(JsonObject json) throws JsonSyntaxException;

  /**
   * Serializes the passed object into the JSON instance
   * @param json    JSON instance
   * @param parent  Object
   */
  void serialize(P parent, JsonObject json);

  /** Parses this loadable from the network */
  T fromNetwork(FriendlyByteBuf buffer);

  /** Writes this loadable to the network */
  void toNetwork(P parent, FriendlyByteBuf buffer);
}

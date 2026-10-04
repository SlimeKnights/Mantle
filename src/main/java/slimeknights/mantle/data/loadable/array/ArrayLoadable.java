package slimeknights.mantle.data.loadable.array;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.loadable.ErrorFactory;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.function.Function;

/** Helpers for all array loadables */
public interface ArrayLoadable<A> extends Loadable<A> {
  /** Special size representing compact where empty is allowed */
  int COMPACT_OR_EMPTY = -2;
  /** Special size representing comapct where empty is disallowed */
  int COMPACT = -1;

  /** If true, this loadable allows compact */
  boolean allowCompact();

  /** Validates the size is correct */
  void checkSize(String key, int size, ErrorFactory error);

  /** Gets the length of the array */
  int getLength(A array);


  /** Converts the given element into a length 1 array  */
  A convertCompact(JsonElement element, String key, TypedMap context);

  /** Converts the given array into an element array */
  A convertArray(JsonArray array, String key, TypedMap context);


  /** Serializes the first elements into a JSON element */
  JsonElement serializeFirst(A object, TypedMap context);

  /** Serializes all elements into the passed JSON array*/
  void serializeAll(JsonArray array, A object, TypedMap context);


  /* Implementation */

  @Override
  default A convert(JsonElement element, String key, TypedMap context) {
    // if we allow compact, parse from compact
    if (allowCompact() && !element.isJsonArray()) {
      return convertCompact(element, key, context);
    }
    JsonArray array = GsonHelper.convertToJsonArray(element, key);
    checkSize(key, array.size(), ErrorFactory.JSON_SYNTAX_ERROR);
    return convertArray(array, key, context);
  }


  @Override
  default JsonElement serialize(A object, TypedMap context) {
    // if we support compact, serialize compact
    int length = getLength(object);
    if (allowCompact() && length == 1) {
      JsonElement element = serializeFirst(object, context);
      // only return if its not an array; arrays means a conflict with deserializing
      // there is a small waste of work here in the case of array, but you shouldn't be using compact with array serializing elements anyway
      if (!element.isJsonArray()) {
        return element;
      }
    }
    checkSize("Collection", length, ErrorFactory.RUNTIME);
    JsonArray array = new JsonArray();
    serializeAll(array, object, context);
    return array;
  }


  /* Fields */

  /** Creates a field that defaults to empty */
  <P> LoadableField<A,P> emptyField(String key, boolean serializeEmpty, Function<P,A> getter);

  /** Creates a field that defaults to empty */
  default <P> LoadableField<A,P> emptyField(String key, Function<P,A> getter) {
    return emptyField(key, false, getter);
  }

  /** Standard implementation of array loadable using a single size parameter */
  interface SizeRange<A> extends ArrayLoadable<A> {
    /** Gets the minimum size */
    int minSize();

    /** Gets the maximum size */
    int maxSize();

    @Override
    default boolean allowCompact() {
      return minSize() < 0;
    }

    @Override
    default void checkSize(String key, int size, ErrorFactory error) {
      // ensure compact min size is displayed as the proper value
      int minSize = minSize();
      if (minSize == COMPACT) {
        minSize = 1;
      } else {
        minSize = 0;
      }
      int maxSize = maxSize();
      if (size < minSize || maxSize < size) {
        if (maxSize == Integer.MAX_VALUE) {
          throw error.create(key + " must have at least " + minSize + " elements");
        } else {
          throw error.create(key + " must have between " + minSize + " and " + maxSize + " elements");
        }
      }
    }
  }
}

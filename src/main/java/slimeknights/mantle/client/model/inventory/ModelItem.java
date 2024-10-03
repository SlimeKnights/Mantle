package slimeknights.mantle.client.model.inventory;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.util.JsonHelper;

import java.util.List;

public class ModelItem {
  /** Model item for rendering no item */
  private static final ModelItem EMPTY = new ModelItem(new Vector3f(0, 0, 0), 0, 0, 0);

  /** Item center location in pixels */
  @Getter
  private final Vector3f center;
  /** Item size in pixels. If 0, item is skipped */
  @Getter
  private final float size;
  /** X axis rotation, applied first */
  @Getter
  private final float x;
  /** Y axis rotation, applied second */
  @Getter
  private final float y;
  @Getter
  private final TransformType transform;

  /** Item center location in percentages, lazy loaded */
  private Vector3f centerScaled;
  /** Item size in percentages, lazy loaded */
  private Float sizeScaled;

  public ModelItem(Vector3f center, float size, float x, float y) {
    this(center, size, x, y, TransformType.NONE);
  }

  public ModelItem(Vector3f center, float size, float x, float y, TransformType transform) {
    this.center = center;
    this.size = size;
    this.x = x;
    this.y = y;
    this.transform = transform;
  }

  /**
   * Gets the center for rendering this item, scaled for renderer
   * @return Scaled center
   */
  public Vector3f getCenterScaled() {
    if (centerScaled == null) {
      centerScaled = new Vector3f(center);
      centerScaled.mul(1f / 16f);
    }
    return centerScaled;
  }

  /**
   * Gets the size to render this item, scaled for the renderer
   * @return Size scaled
   */
  public float getSizeScaled() {
    if (sizeScaled == null) {
      sizeScaled = size / 16f;
    }
    return sizeScaled;
  }

  /**
   * Returns true if this model item is hidden, meaning no items should be rendered
   * @return  True if hidden
   */
  public boolean isHidden() {
    return size == 0;
  }

  /** Parses a transform type from a string */
  private static TransformType parseTransformType(JsonObject json, String key) {
    String name = GsonHelper.getAsString(json, key, "none");
    for (TransformType type : TransformType.values()) {
      if (name.equals(type.getSerializeName())) {
        return type;
      }
    }
    throw new JsonSyntaxException("Unknown transform type " + name);
  }

  /**
   * Gets a model item from a JSON object
   * @param json  JSON object instance
   * @return  Model item object
   */
  public static ModelItem fromJson(JsonObject json) {
    // if the size is 0, skip rendering this item
    float size = GsonHelper.getAsFloat(json, "size");
    if (size == 0) {
      return ModelItem.EMPTY;
    }
    Vector3f center = ModelHelper.arrayToVector(json, "center");
    float x = ModelHelper.getRotation(json, "x");
    float y = ModelHelper.getRotation(json, "y");
    TransformType transformType = parseTransformType(json, "transform");
    return new ModelItem(center, size, x, y, transformType);
  }

  /**
   * Gets a list of model items from JSON
   * @param parent  Parent JSON object
   * @param key     Name of the array of model item objects
   * @return  List of model items
   */
  public static List<ModelItem> listFromJson(JsonObject parent, String key) {
    return JsonHelper.parseList(parent, key, ModelItem::fromJson);
  }
}

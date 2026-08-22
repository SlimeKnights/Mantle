package slimeknights.mantle.client.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.data.datamap.BlockStateDataMapLoader;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.DisplayContextLoadable;
import slimeknights.mantle.data.loadable.common.Vector3fLoadable;
import slimeknights.mantle.data.loadable.mapping.CollectionLoadable;
import slimeknights.mantle.data.loadable.primitive.FloatLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import java.util.List;

/** Utility for placing items in block entity renderers */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RenderItem {
  /** Model item for rendering no item */
  private static final RenderItem EMPTY = new RenderItem(new Vector3f(0, 0, 0), 0, 0, 0, ItemDisplayContext.NONE);
  /** Loader instance */
  public static final RecordLoadable<RenderItem> LOADABLE = RecordLoadable.create(
    Vector3fLoadable.INSTANCE.defaultField("center", EMPTY.center, RenderItem::getCenter),
    FloatLoadable.FROM_ZERO.requiredField("size", RenderItem::getSize),
    Loadables.ROTATION.defaultField("x", 0, RenderItem::getX),
    Loadables.ROTATION.defaultField("y", 0, RenderItem::getY),
    DisplayContextLoadable.INSTANCE.defaultField("transform", ItemDisplayContext.NONE, RenderItem::getTransform),
    RenderItem::new).validate((value, error) -> {
      if (value.size == 0) {
        return EMPTY;
      }
      return value;
    });
  private static final Loadable<List<RenderItem>> LIST_LOADABLE = LOADABLE.list(CollectionLoadable.COMPACT);
  /** Data loader to fetch a list of fluid cuboids from JSON */
  public static final BlockStateDataMapLoader<List<RenderItem>> REGISTRY = new BlockStateDataMapLoader<>("Block entity items", "mantle/model/item_lists", LIST_LOADABLE);

  /** Item center location in pixels */
  @Getter
  private final Vector3f center;
  /** Item size in pixels. If 0, item is skipped */
  @Getter
  private final float size;
  /** X axis rotation, applied first */
  @Getter
  private final int x;
  /** Y axis rotation, applied second */
  @Getter
  private final int y;
  @Getter
  private final ItemDisplayContext transform;

  /** Item center location in percentages, lazy loaded */
  private Vector3f centerScaled;
  /** Item size in percentages, lazy loaded */
  private Float sizeScaled;

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


  /** Creates a new builder */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder logic */
  @Accessors(fluent = true)
  public static class Builder {
    private Vector3f center = new Vector3f(8, 8, 8);
    @Setter
    private float size = 16;
    private int x = 0;
    private int y = 0;
    @Setter
    private ItemDisplayContext transform = ItemDisplayContext.NONE;

    /** Sets the center */
    public Builder center(float x, float y, float z) {
      this.center = new Vector3f(x, y, z);
      return this;
    }

    /** Sets the X rotation */
    public Builder x(int x) {
      if (!ModelHelper.checkRotation(x)) {
        throw new IllegalArgumentException("Invalid rotation value " + x + ", must be 0/90/180/270");
      }
      this.x = x;
      return this;
    }

    /** Sets the Y rotation */
    public Builder y(int y) {
      if (!ModelHelper.checkRotation(y)) {
        throw new IllegalArgumentException("Invalid rotation value " + y + ", must be 0/90/180/270");
      }
      this.y = y;
      return this;
    }

    /** Builds the final instance */
    public RenderItem build() {
      return new RenderItem(center, size, x, y, transform);
    }
  }
}

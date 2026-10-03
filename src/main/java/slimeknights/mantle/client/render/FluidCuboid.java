package slimeknights.mantle.client.render;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.data.datamap.BlockStateDataMapLoader;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.Vector3fLoadable;
import slimeknights.mantle.data.loadable.mapping.CollectionLoadable;
import slimeknights.mantle.data.loadable.primitive.BooleanLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Class representing a cube of fluid, which appears in many model types. */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FluidCuboid {
  protected static final Map<Direction, FluidFace> DEFAULT_FACES;
  static {
    DEFAULT_FACES = new EnumMap<>(Direction.class);
    for (Direction direction : Direction.values()) {
      DEFAULT_FACES.put(direction, FluidFace.NORMAL);
    }
  }
  /** Loader instance */
  public static final RecordLoadable<FluidCuboid> LOADABLE = RecordLoadable.create(
    Vector3fLoadable.INSTANCE.requiredField("from", FluidCuboid::getFrom),
    Vector3fLoadable.INSTANCE.requiredField("to", FluidCuboid::getTo),
    Loadables.DIRECTION.mapWithValues(FluidFace.LOADABLE, 1).defaultField("faces", DEFAULT_FACES, FluidCuboid::getFaces),
    FluidCuboid::new);
  /** Loader for a list of size 1 */
  public static final Loadable<List<FluidCuboid>> LIST_LOADABLE = LOADABLE.list(CollectionLoadable.COMPACT);
  /** Data loader to fetch a list of fluid cuboids from JSON */
  public static final BlockStateDataMapLoader<List<FluidCuboid>> REGISTRY = new BlockStateDataMapLoader<>("Block entity fluids", "mantle/model/block_fluids", LIST_LOADABLE);

  /** Fluid start, scaled for block models */
  @Getter
  private final Vector3f from;
  /** Fluid end, scaled for block models */
  @Getter
  private final Vector3f to;
  /** Block faces for the fluid */
  @Getter
  private final Map<Direction, FluidFace> faces;

  /** Cache for scaled from */
  @Nullable
  private Vector3f fromScaled;
  /** Cache for scaled to */
  @Nullable
  private Vector3f toScaled;

  /**
   * Checks if the fluid has the given face
   * @param face  Face to check
   * @return  True if the face is present
   */
  @Nullable
  public FluidFace getFace(Direction face) {
    return faces.get(face);
  }

  /**
   * Gets fluid from, scaled for renderer
   * @return Scaled from
   */
  public Vector3f getFromScaled() {
    if (fromScaled == null) {
      fromScaled = new Vector3f(from);
      fromScaled.mul(1 / 16f);
    }
    return fromScaled;
  }

  /**
   * Gets fluid to, scaled for renderer
   * @return Scaled from
   */
  public Vector3f getToScaled() {
    if (toScaled == null) {
      toScaled = new Vector3f(to);
      toScaled.mul(1 / 16f);
    }
    return toScaled;
  }

  /** Represents a single fluid face in the model */
  public record FluidFace(boolean isFlowing, int rotation) {
    public static final FluidFace NORMAL = new FluidFace(false, 0);
    public static final RecordLoadable<FluidFace> LOADABLE = RecordLoadable.create(
      BooleanLoadable.INSTANCE.defaultField("flowing", false, false, FluidFace::isFlowing),
      Loadables.ROTATION.defaultField("rotation", 0, FluidFace::rotation),
      FluidFace::new);

    public FluidFace {
      if (!ModelHelper.checkRotation(rotation)) {
        throw new IllegalArgumentException("Rotation must be 0/90/180/270");
      }
    }
  }


  /** Creates a new builder instance */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder logic */
  @SuppressWarnings("unused")  // API
  @CanIgnoreReturnValue
  public static class Builder {
    private Vector3f from = new Vector3f(0, 0, 0);
    private Vector3f to = new Vector3f(16, 16, 16);
    private final Map<Direction,FluidFace> faces = new EnumMap<>(Direction.class);

    /** Sets the starting bounds */
    public Builder from(float x, float y, float z) {
      this.from = new Vector3f(x, y, z);
      return this;
    }

    /** Sets the ending bounds */
    public Builder to(float x, float y, float z) {
      this.to = new Vector3f(x, y, z);
      return this;
    }

    /** Adds a face to the builder */
    private Builder face(Direction direction, FluidFace face) {
      FluidFace oldFace = faces.putIfAbsent(direction, face);
      if (oldFace != null) {
        throw new IllegalArgumentException("Duplicate face for " + direction + ", original " + oldFace + ", new " + face);
      }
      return this;
    }

    /** Adds a face to the builder */
    public Builder face(boolean flowing, int rotation, Direction direction, Direction... others) {
      FluidFace face = new FluidFace(flowing, rotation);
      for (Direction other : others) {
        face(other, face);
      }
      return face(direction, face);
    }

    /** Adds a face to the builder */
    public Builder face(Direction direction, Direction... others) {
      for (Direction other : others) {
        face(other, FluidFace.NORMAL);
      }
      return face(direction, FluidFace.NORMAL);
    }

    /** Builds the final instance */
    @CheckReturnValue
    public FluidCuboid build() {
      return new FluidCuboid(from, to, faces.isEmpty() ? DEFAULT_FACES : faces);
    }
  }
}

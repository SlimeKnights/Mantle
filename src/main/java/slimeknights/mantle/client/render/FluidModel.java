package slimeknights.mantle.client.render;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Vector3f;
import slimeknights.mantle.data.datamap.BlockStateDataMapLoader;
import slimeknights.mantle.data.loadable.Loadable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a model for {@link FluidCuboid}. Caches some properties used for tank bounds.
 * @see FluidRenderer#renderScaledModel(PoseStack, MultiBufferSource, FluidModel, FluidStack, float, int, int, boolean) 
 * @see FluidRenderer#renderCuboids(PoseStack, VertexConsumer, List, FluidStack, int)
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class FluidModel {
  /** Default instance with no fluids */
  public static final FluidModel EMPTY = new FluidModel();
  /** Loadable for a fluid model */
  public static final Loadable<FluidModel> LOADABLE = FluidCuboid.LIST_LOADABLE.map(FluidModel::of, FluidModel::getFluids);
  /** Data loader to fetch a list of fluid cuboids from JSON */
  public static final BlockStateDataMapLoader<FluidModel> REGISTRY = new BlockStateDataMapLoader<>("Block entity fluids", "mantle/model/block_fluids", LOADABLE);

  /** Gets the model for a given state, or {@link #EMPTY} if absent. */
  public static FluidModel getModel(BlockState state) {
    return REGISTRY.get(state, EMPTY);
  }

  /** Gets the fluid list for a given state, or an empty list if absent. */
  public static List<FluidCuboid> getFluids(BlockState state) {
    return getModel(state).getFluids();
  }


  @Getter
  private final List<FluidCuboid> fluids;
  /** Minimum values out of any fluid cuboid in the model */
  @Getter
  private final Vector3f min;
  /** Maximum values out of any fluid cuboid in the model */
  @Getter
  private final Vector3f max;
  /** Cache for scaled minimum */
  private Vector3f minScaled;
  /** Cache for scaled maximum */
  private Vector3f maxScaled;

  /** Creates the empty instance */
  private FluidModel() {
    this.fluids = List.of();
    Vector3f zero = new Vector3f(0f, 0f, 0f);
    this.min = zero;
    this.max = zero;
    this.minScaled = zero;
    this.maxScaled = zero;
  }

  /** Creates a new fluid model */
  public static FluidModel of(List<FluidCuboid> fluids) {
    if (fluids.isEmpty()) {
      return EMPTY;
    }
    // calculate bounds
    float minX = 0;
    float minY = 0;
    float minZ = 0;
    float maxX = 16;
    float maxY = 16;
    float maxZ = 16;
    for (FluidCuboid fluid : fluids) {
      Vector3f from = fluid.getFrom();
      Vector3f to = fluid.getTo();
      minX = Math.min(minX, from.x());
      minY = Math.min(minY, from.y());
      minZ = Math.min(minZ, from.z());
      maxX = Math.max(maxX, to.x());
      maxY = Math.max(maxY, to.y());
      maxZ = Math.max(maxZ, to.z());
    }
    return new FluidModel(
      fluids,
      new Vector3f(minX, minY, minZ),
      new Vector3f(maxX, maxY, maxZ)
    );
  }

  /** Checks if this model is empty */
  public boolean isEmpty() {
    return fluids.isEmpty();
  }

  /**
   * Gets fluid minimum bounds, scaled for renderer
   * @return Scaled min bounds
   */
  public Vector3f getMinScaled() {
    if (minScaled == null) {
      minScaled = new Vector3f(min);
      minScaled.mul(1 / 16f);
    }
    return minScaled;
  }

  /**
   * Gets fluid maximum bounds, scaled for renderer
   * @return Scaled max bounds
   */
  public Vector3f getMaxScaled() {
    if (maxScaled == null) {
      maxScaled = new Vector3f(max);
      maxScaled.mul(1 / 16f);
    }
    return maxScaled;
  }


  /* Builder */

  /** Creates a new builder instance */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for a model */
  @CanIgnoreReturnValue
  public static class Builder {
    private final List<FluidCuboid> fluids = new ArrayList<>();

    private Builder() {}

    /** Adds a fluid to the builder */
    public Builder fluid(FluidCuboid fluid) {
      fluids.add(fluid);
      return this;
    }

    /** Adds a fluid to the builder */
    public Builder fluid(FluidCuboid.Builder fluid) {
      return fluid(fluid.build());
    }

    /** Creates the final model */
    @CheckReturnValue
    public FluidModel build() {
      return FluidModel.of(List.copyOf(fluids));
    }
  }
}

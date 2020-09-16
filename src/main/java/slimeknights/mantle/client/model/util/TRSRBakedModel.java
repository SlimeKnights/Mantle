package slimeknights.mantle.client.model.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.VertexTransformer;
import net.minecraftforge.common.model.TransformationHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Model that Translates, Rotates, Scales, then Rotates a child model
 * TODO: is this still needed?
 */
public class TRSRBakedModel extends DynamicBakedWrapper<IBakedModel> {

  private final TransformationMatrix transformation;
  private final TRSROverride override;
  private final int faceOffset;

  public TRSRBakedModel(IBakedModel original, float x, float y, float z, float scale) {
    this(original, x, y, z, 0, 0, 0, scale, scale, scale);
  }

  public TRSRBakedModel(IBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scale) {
    this(original, x, y, z, rotX, rotY, rotZ, scale, scale, scale);
  }

  public TRSRBakedModel(IBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scaleX, float scaleY, float scaleZ) {
    this(original, new TransformationMatrix(new Vector3f(x, y, z),
            null,
            new Vector3f(scaleX, scaleY, scaleZ),
            TransformationHelper.quatFromXYZ(new float[] { rotX, rotY, rotZ }, false)));
  }

  public TRSRBakedModel(IBakedModel original, TransformationMatrix transform) {
    super(original);
    this.transformation = transform.blockCenterToCorner();
    this.override = new TRSROverride(this);
    this.faceOffset = 0;
  }

  /** Rotates around the Y axis and adjusts culling appropriately. South is default. */
  public TRSRBakedModel(IBakedModel original, Direction facing) {
    super(original);
    this.override = new TRSROverride(this);

    this.faceOffset = 4 + Direction.NORTH.getHorizontalIndex() - facing.getHorizontalIndex();

    double r = Math.PI * (360 - facing.getOpposite().getHorizontalIndex() * 90) / 180d;
    this.transformation = new TransformationMatrix(null, null, null, TransformationHelper.quatFromXYZ(new float[] { 0, (float) r, 0 }, false)).blockCenterToCorner();
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data) {
    // transform quads obtained from parent

    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
    if (!this.originalModel.isBuiltInRenderer()) {
      try {
        // adjust side to facing-rotation
        if (side != null && side.getHorizontalIndex() > -1) {
          side = Direction.byHorizontalIndex((side.getHorizontalIndex() + this.faceOffset) % 4);
        }
        for (BakedQuad quad : this.originalModel.getQuads(state, side, rand, data)) {
          Transformer transformer = new Transformer(this.transformation, quad.getSprite());
          quad.pipe(transformer);
          builder.add(transformer.build());
        }
      }
      catch (Exception e) {
        // do nothing. Seriously, why are you using immutable lists?!
      }
    }

    return builder.build();
  }

  @Override
  public ItemOverrideList getOverrides() {
    return this.override;
  }

  private static class TRSROverride extends ItemOverrideList {

    private final TRSRBakedModel model;

    TRSROverride(TRSRBakedModel model) {
      this.model = model;
    }

    @Nullable
    @Override
    public IBakedModel getOverrideModel(IBakedModel originalModel, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
      IBakedModel baked = this.model.originalModel.getOverrides().getOverrideModel(originalModel, stack, world, entity);
      if (baked == null) {
        return null;
      }
      return new TRSRBakedModel(baked, this.model.transformation);
    }
  }

  private static class Transformer extends VertexTransformer {

    protected Matrix4f transformation;
    protected Matrix3f normalTransformation;

    public Transformer(TransformationMatrix transformation, TextureAtlasSprite textureAtlasSprite) {
      super(new BakedQuadBuilder(textureAtlasSprite));
      // position transform
      this.transformation = transformation.getMatrix();
      // normal transform
      this.normalTransformation = new Matrix3f(this.transformation);
      this.normalTransformation.invert();
      this.normalTransformation.transpose();
    }

    @Override
    public void put(int element, float... data) {
      VertexFormatElement.Usage usage = this.parent.getVertexFormat().getElements().get(element).getUsage();

      // transform normals and position
      if (usage == VertexFormatElement.Usage.POSITION && data.length >= 3) {
        Vector4f vec = new Vector4f(data[0], data[1], data[2], 1f);
        vec.transform(this.transformation);
        data = new float[4];
        data[0] = vec.getX();
        data[1] = vec.getY();
        data[2] = vec.getZ();
        data[3] = vec.getW();
      }
      else if (usage == VertexFormatElement.Usage.NORMAL && data.length >= 3) {
        Vector3f vec = new Vector3f(data);
        vec.transform(this.normalTransformation);
        vec.normalize();
        data = new float[4];
        data[0] = vec.getX();
        data[1] = vec.getY();
        data[2] = vec.getZ();
      }
      super.put(element, data);
    }

    public BakedQuad build() {
      return ((BakedQuadBuilder) this.parent).build();
    }
  }
}

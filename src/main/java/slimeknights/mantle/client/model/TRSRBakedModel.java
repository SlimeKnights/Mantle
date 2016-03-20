package slimeknights.mantle.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexTransformer;

import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

// for those wondering TRSR stands for Translation Rotation Scale Rotation
public class TRSRBakedModel implements IBakedModel {

  protected final IBakedModel original;
  protected final TRSRTransformation transformation;
  private final TRSROverride override;
  private final int faceOffset;

  public TRSRBakedModel(IBakedModel original, float x, float y, float z, float scale) {
    this(original, x, y, z, 0, 0, 0, scale, scale, scale);
  }

  public TRSRBakedModel(IBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scale) {
    this(original, x, y, z, rotX, rotY, rotZ, scale, scale, scale);
  }

  public TRSRBakedModel(IBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scaleX, float scaleY, float scaleZ) {
    this(original, new TRSRTransformation(new Vector3f(x, y, z),
                                          null,
                                          new Vector3f(scaleX, scaleY, scaleZ),
                                          TRSRTransformation.quatFromXYZ(rotX, rotY, rotZ)));
  }

  public TRSRBakedModel(IBakedModel original, TRSRTransformation transform) {
    this.original = original;
    this.transformation = TRSRTransformation.blockCenterToCorner(transform);
    this.override = new TRSROverride(this);
    this.faceOffset = 0;
  }

  /** Rotates around the Y axis and adjusts culling appropriately. South is default. */
  public TRSRBakedModel(IBakedModel original, EnumFacing facing) {
    this.original = original;
    this.override = new TRSROverride(this);

    this.faceOffset = 4 + EnumFacing.NORTH.getHorizontalIndex() - facing.getHorizontalIndex();

    double r = Math.PI * (360 - facing.getOpposite().getHorizontalIndex() * 90)/180d;
    TRSRTransformation t = new TRSRTransformation(null, null, null, TRSRTransformation.quatFromXYZ(0, (float)r, 0));
    this.transformation = TRSRTransformation.blockCenterToCorner(t);
  }

  @Override
  public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
    // transform quads obtained from parent

    ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

    if(!original.isBuiltInRenderer()) {
      // adjust side to facing-rotation
      if(side != null && side.getHorizontalIndex() > -1) {
        side = EnumFacing.getHorizontal((side.getHorizontalIndex() + faceOffset) % 4);
      }
      for(BakedQuad quad : original.getQuads(state, side, rand)) {
        Transformer transformer = new Transformer(transformation, quad.getFormat());
        quad.pipe(transformer);
        builder.add(transformer.build());
      }
    }

    return builder.build();
  }

  @Override
  public boolean isAmbientOcclusion() {
    return false;
  }

  @Override
  public boolean isGui3d() {
    return original.isGui3d();
  }

  @Override
  public boolean isBuiltInRenderer() {
    return original.isBuiltInRenderer();
  }

  @Override
  public TextureAtlasSprite getParticleTexture() {
    return original.getParticleTexture();
  }

  @Override
  public ItemCameraTransforms getItemCameraTransforms() {
    return original.getItemCameraTransforms();
  }

  @Override
  public ItemOverrideList getOverrides() {
    return override;
  }

  private static class TRSROverride extends ItemOverrideList {

    private final TRSRBakedModel model;

    public TRSROverride(TRSRBakedModel model) {
      super(ImmutableList.<ItemOverride>of());

      this.model = model;
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
      IBakedModel baked = model.original.getOverrides().handleItemState(originalModel, stack, world, entity);

      return new TRSRBakedModel(baked, model.transformation);
    }
  }

  private static class Transformer extends VertexTransformer {

    protected Matrix4f transformation;
    protected Matrix3f normalTransformation;

    public Transformer(TRSRTransformation transformation, VertexFormat format) {
      super(new UnpackedBakedQuad.Builder(format));
      // position transform
      this.transformation = transformation.getMatrix();
      // normal transform
      this.normalTransformation = new Matrix3f();
      this.transformation.getRotationScale(this.normalTransformation);
      this.normalTransformation.invert();
      this.normalTransformation.transpose();
    }

    @Override
    public void put(int element, float... data) {
      VertexFormatElement.EnumUsage usage = parent.getVertexFormat().getElement(element).getUsage();

      // transform normals and position
      if(usage == VertexFormatElement.EnumUsage.POSITION && data.length >= 3) {
        Vector4f vec = new Vector4f(data);
        vec.setW(1.0f);
        transformation.transform(vec);
        data = new float[4];
        vec.get(data);
      }
      else if(usage == VertexFormatElement.EnumUsage.NORMAL && data.length >= 3) {
        Vector3f vec = new Vector3f(data);
        normalTransformation.transform(vec);
        vec.normalize();
        data = new float[4];
        vec.get(data);
      }
      super.put(element, data);
    }

    public UnpackedBakedQuad build() {
      return ((UnpackedBakedQuad.Builder) parent).build();
    }
  }
}

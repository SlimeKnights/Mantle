package slimeknights.mantle.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexTransformer;

import java.util.EnumMap;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

// for those wondering TRSR stands for Translation Rotation Scale Rotation
public class TRSRBakedModel implements IFlexibleBakedModel {

  protected final ImmutableList<BakedQuad> general;
  protected final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> faces;
  protected final IFlexibleBakedModel original;

  public TRSRBakedModel(IFlexibleBakedModel original, float x, float y, float z, float scale) {
    this(original, x, y, z, 0, 0, 0, scale, scale, scale);
  }

  public TRSRBakedModel(IFlexibleBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scale) {
    this(original, x, y, z, rotX, rotY, rotZ, scale, scale, scale);
  }

  public TRSRBakedModel(IFlexibleBakedModel original, float x, float y, float z, float rotX, float rotY, float rotZ, float scaleX, float scaleY, float scaleZ) {
    this(original, new TRSRTransformation(new Vector3f(x, y, z),
                                          null,
                                          new Vector3f(scaleX, scaleY, scaleZ),
                                          TRSRTransformation.quatFromYXZ(rotY, rotX, rotZ)));
  }

  public TRSRBakedModel(IFlexibleBakedModel original, TRSRTransformation transform) {
    this.original = original;

    ImmutableList.Builder<BakedQuad> builder;
    builder = ImmutableList.builder();

    transform = TRSRTransformation.blockCenterToCorner(transform);

    // face quads
    EnumMap<EnumFacing, ImmutableList<BakedQuad>> faces = Maps.newEnumMap(EnumFacing.class);
    for(EnumFacing face : EnumFacing.values()) {
      for(BakedQuad quad : original.getFaceQuads(face)) {
        Transformer transformer = new Transformer(transform, original.getFormat());
        quad.pipe(transformer);
        builder.add(transformer.build());
      }
      //faces.put(face, builder.build());
      faces.put(face, ImmutableList.<BakedQuad>of());
    }

    // general quads
    //builder = ImmutableList.builder();
    for(BakedQuad quad : original.getGeneralQuads()) {
      Transformer transformer = new Transformer(transform, original.getFormat());
      quad.pipe(transformer);
      builder.add(transformer.build());
    }

    this.general = builder.build();
    this.faces = Maps.immutableEnumMap(faces);
  }

  @Override
  public List<BakedQuad> getFaceQuads(EnumFacing side) {
    return faces.get(side);
  }

  @Override
  public List<BakedQuad> getGeneralQuads() {
    return general;
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
  public TextureAtlasSprite getTexture() {
    return original.getTexture();
  }

  @Override
  public ItemCameraTransforms getItemCameraTransforms() {
    return original.getItemCameraTransforms();
  }

  @Override
  public VertexFormat getFormat() {
    return original.getFormat();
  }

  public static class Transformer extends VertexTransformer {

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
      if(usage == VertexFormatElement.EnumUsage.POSITION) {
        Vector4f vec = new Vector4f(data);
        vec.setW(1.0f);
        transformation.transform(vec);
        data = new float[4];
        vec.get(data);
      }
      else if(usage == VertexFormatElement.EnumUsage.NORMAL) {
        Vector3f vec = new Vector3f(data);
        normalTransformation.transform(vec);
        vec.normalize();
        data = new float[4];
        vec.get(data);
      }
      super.put(element, data);
    }

    public UnpackedBakedQuad build() {
      return ((UnpackedBakedQuad.Builder)parent).build();
    }
  }
}

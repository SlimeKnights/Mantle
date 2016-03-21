package slimeknights.mantle.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import java.util.List;

import slimeknights.mantle.util.ImmutableConcatList;

public class BakedCompositeModel extends BakedWrapper {

  protected final List<IBakedModel> parts;

  public BakedCompositeModel(IBakedModel parent, List<IBakedModel> parts) {
    super(parent);
    this.parts = parts;
  }

  @Override
  public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
    ImmutableList.Builder<List<BakedQuad>> builder = ImmutableList.builder();

    builder.add(super.getQuads(state, side, rand));

    for(IBakedModel part : parts) {
      builder.add(part.getQuads(state, side, rand));
    }

    return new ImmutableConcatList<BakedQuad>(builder.build());
  }
}

package slimeknights.mantle.client.model;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.IntData;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import java.util.List;
import java.util.Map;

import slimeknights.mantle.util.ImmutableConcatList;

/**
 * Groups multiple baked models into a single one. Does not respect state etc. when getting quads.
 * Best use the Builder to create a BakedCompositeModel.
 */
public class BakedCompositeModel extends BakedWrapper {

  protected final ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> parts;

  public BakedCompositeModel(IBakedModel parent, ImmutableMap<Optional<EnumFacing>, ImmutableList<BakedQuad>> parts) {
    super(parent);

    this.parts = parts;
  }

  @Override
  public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
    return parts.get(Optional.fromNullable(side));
  }

  public static class Builder {
    ImmutableList.Builder<BakedQuad>[] builders;
    IBakedModel parent;

    public Builder() {
      this.parent = parent;
      builders = new ImmutableList.Builder[7];
      for(int i = 0; i < 7; i++) {
        builders[i] = ImmutableList.builder();
      }
    }

    public void add(IBakedModel model, IBlockState state, long rand) {
      add(model, state, null, rand);
      for(EnumFacing side : EnumFacing.values()) {
        add(model, state, side, rand);
      }
    }

    public void add(IBakedModel model, IBlockState state, EnumFacing side, long rand) {
      int index;
      if(side == null) {
        index = 6;
      }
      else {
        index = side.getIndex();
      }

      builders[index].addAll(model.getQuads(state, side, rand));
    }

    public BakedCompositeModel build(IBakedModel parent) {
      ImmutableMap.Builder<Optional<EnumFacing>, ImmutableList<BakedQuad>> map = ImmutableMap.builder();

      map.put(Optional.<EnumFacing>absent(), builders[6].build());
      for(EnumFacing side : EnumFacing.values()) {
        map.put(Optional.of(side), builders[side.getIndex()].build());
      }

      return new BakedCompositeModel(parent, map.build());
    }
  }
}

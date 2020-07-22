package slimeknights.mantle.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Groups multiple baked models into a single one. Does not respect state etc. when getting quads.
 * Best use the Builder to create a BakedCompositeModel.
 */
public class BakedCompositeModel extends BakedWrapper {

  protected final ImmutableMap<Optional<Direction>, ImmutableList<BakedQuad>> parts;

  public BakedCompositeModel(IBakedModel parent, ImmutableMap<Optional<Direction>, ImmutableList<BakedQuad>> parts) {
    super(parent);

    this.parts = parts;
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData) {
    return this.parts.get(Optional.ofNullable(side));
  }

  public static class Builder {

    ImmutableList.Builder<BakedQuad>[] builders;
    IBakedModel parent;

    public Builder() {
      this.builders = new ImmutableList.Builder[7];
      for (int i = 0; i < 7; i++) {
        this.builders[i] = ImmutableList.builder();
      }
    }

    public void add(IBakedModel model, BlockState state, Random rand) {
      this.add(model, state, null, rand);
      for (Direction side : Direction.values()) {
        this.add(model, state, side, rand);
      }
    }

    public void add(IBakedModel model, BlockState state, @Nullable Direction side, Random rand) {
      int index;
      if (side == null) {
        index = 6;
      }
      else {
        index = side.getIndex();
      }

      this.builders[index].addAll(model.getQuads(state, side, rand));
    }

    public BakedCompositeModel build(IBakedModel parent) {
      ImmutableMap.Builder<Optional<Direction>,ImmutableList<BakedQuad>> map = ImmutableMap.builder();

      map.put(Optional.empty(), this.builders[6].build());
      for (Direction side : Direction.values()) {
        map.put(Optional.of(side), this.builders[side.getIndex()].build());
      }

      return new BakedCompositeModel(parent, map.build());
    }
  }
}

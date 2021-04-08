package slimeknights.mantle.client.model.util;

import com.mojang.datafixers.util.Either;
import lombok.AllArgsConstructor;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraftforge.client.model.IModelConfiguration;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

@AllArgsConstructor
public class ModelTextureIteratable implements Iterable<Map<String,Either<SpriteIdentifier, String>>> {
  /** Initial map for iteration */
  @Nullable
  private final Map<String,Either<SpriteIdentifier, String>> startMap;
  /** Initial model for iteration */
  @Nullable
  private final JsonUnbakedModel startModel;

  /**
   * Creates an iterable over the given model
   * @param model  Model
   */
  public ModelTextureIteratable(JsonUnbakedModel model) {
    this(null, model);
  }

  /**
   *
   * @param owner     Model configuration owner
   * @param fallback  Fallback in case the owner does not contain a block model
   * @return  Iteratable over block model texture maps
   */
  public static ModelTextureIteratable of(IModelConfiguration owner, SimpleBlockModel fallback) {
    UnbakedModel unbaked = owner.getOwnerModel();
    if (unbaked instanceof JsonUnbakedModel) {
      return new ModelTextureIteratable(null, (JsonUnbakedModel)unbaked);
    }
    return new ModelTextureIteratable(fallback.getTextures(), fallback.getParent());
  }

  @Override
  public MapIterator iterator() {
    return new MapIterator(startMap, startModel);
  }

  @AllArgsConstructor
  private static class MapIterator implements Iterator<Map<String,Either<SpriteIdentifier, String>>> {
    /** Initial map for iteration */
    @Nullable
    private Map<String,Either<SpriteIdentifier, String>> initial;
    /** current model in the iterator */
    @Nullable
    private JsonUnbakedModel model;

    @Override
    public boolean hasNext() {
      return initial != null || model != null;
    }

    @Override
    public Map<String,Either<SpriteIdentifier,String>> next() {
      Map<String,Either<SpriteIdentifier, String>> map;
      if (initial != null) {
        map = initial;
        initial = null;
      } else if (model != null) {
        map = model.textureMap;
        model = model.parent;
      } else {
        throw new NoSuchElementException();
      }
      return map;
    }
  }
}

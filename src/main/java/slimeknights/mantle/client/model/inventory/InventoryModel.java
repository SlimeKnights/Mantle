package slimeknights.mantle.client.model.inventory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This model contains a list of multiple items to display in a TESR
 */
@AllArgsConstructor
public class InventoryModel implements IUnbakedGeometry<InventoryModel> {
  public static final IGeometryLoader<InventoryModel> LOADER = InventoryModel::deserialize;

  protected final SimpleBlockModel model;
  protected final List<ModelItem> items;

  @Override
  public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    return model.getMaterials(owner, modelGetter, missingTextureErrors);
  }

  @Override
  public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
    BakedModel baked = model.bake(owner, bakery, spriteGetter, transform, overrides, location);
    return new Baked(baked, items);
  }

  /** Baked model, mostly a data wrapper around a normal model */
  @SuppressWarnings("WeakerAccess")
  public static class Baked extends BakedModelWrapper<BakedModel> {
    @Getter
    private final List<ModelItem> items;
    public Baked(BakedModel originalModel, List<ModelItem> items) {
      super(originalModel);
      this.items = items;
    }
  }

  /** Deserializes an inventory model from JSON */
  public static InventoryModel deserialize(JsonObject json, JsonDeserializationContext context) {
    ColoredBlockModel model = ColoredBlockModel.deserialize(json, context);
    List<ModelItem> items = ModelItem.listFromJson(json, "items");
    return new InventoryModel(model, items);
  }
}

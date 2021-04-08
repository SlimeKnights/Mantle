package slimeknights.mantle.client.model.inventory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This model contains a list of multiple items to display in a TESR
 */
@AllArgsConstructor
public class InventoryModel implements IModelGeometry<InventoryModel> {
  protected final SimpleBlockModel model;
  protected final List<ModelItem> items;

  @Override
  public Collection<SpriteIdentifier> getTextures(IModelConfiguration owner, Function<Identifier,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    return model.getTextures(owner, modelGetter, missingTextureErrors);
  }

  @Override
  public net.minecraft.client.render.model.BakedModel bake(IModelConfiguration owner, ModelLoader bakery, Function<SpriteIdentifier,Sprite> spriteGetter, ModelBakeSettings transform, ModelOverrideList overrides, Identifier location) {
    net.minecraft.client.render.model.BakedModel baked = model.bakeModel(owner, transform, overrides, spriteGetter, location);
    return new BakedModel(baked, items);
  }

  /** Baked model, mostly a data wrapper around a normal model */
  @SuppressWarnings("WeakerAccess")
  public static class BakedModel extends BakedModelWrapper<net.minecraft.client.render.model.BakedModel> {
    @Getter
    private final List<ModelItem> items;
    public BakedModel(net.minecraft.client.render.model.BakedModel originalModel, List<ModelItem> items) {
      super(originalModel);
      this.items = items;
    }
  }

  /** Loader for this model */
  public static class Loader implements IModelLoader<InventoryModel> {
    /**
     * Shared loader instance
     */
    public static final Loader INSTANCE = new Loader();

    @Override
    public void apply(ResourceManager resourceManager) {}

    @Override
    public InventoryModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
      List<ModelItem> items = ModelItem.listFromJson(modelContents, "items");
      return new InventoryModel(model, items);
    }
  }
}

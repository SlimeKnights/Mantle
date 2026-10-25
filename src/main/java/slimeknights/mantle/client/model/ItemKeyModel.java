package slimeknights.mantle.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.geometry.BlockGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.util.ModelTextureIteratable;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/** Model which uses a key in item components to select which texture variant to load. */
@RequiredArgsConstructor
public class ItemKeyModel implements IUnbakedGeometry<ItemKeyModel> {
  /** Model loader ID */
  public static final ResourceLocation ID = Mantle.getResource("item_key");
  /** Model loader instance */
  public static final IGeometryLoader<ItemKeyModel> LOADER = ItemKeyModel::deserialize;

  /** Key getter */
  private final ItemKey key;
  /** Key denoting which extra textures to fetch from the map */
  @Nullable
  private final ResourceLocation extraTexturesKey;

  /** Deserializes this model from JSON */
  public static ItemKeyModel deserialize(JsonObject json, JsonDeserializationContext context) {
    ItemKey key = ItemKey.LOADER.getIfPresent(json, "nbt_key");
    ResourceLocation extraTexturesKey = null;
    if (json.has("extra_textures_key")) {
      extraTexturesKey = JsonHelper.getResourceLocation(json, "extra_textures_key");
    }
    return new ItemKeyModel(key, extraTexturesKey);
  }

  /** Map of textures for the model */
  private Map<String,Material> textures = Collections.emptyMap();

  @Override
  public void resolveParents(Function<ResourceLocation,UnbakedModel> modelGetter, IGeometryBakingContext owner) {
    textures = new HashMap<>();
    // must have a default
    Material defaultTexture = owner.getMaterial("default");
    textures.put("default", defaultTexture);
    // fetch others, not sure if there is a better way to get all defined textures
    if (owner instanceof BlockGeometryBakingContext blockContext) {
      ModelTextureIteratable iterable = new ModelTextureIteratable(null, blockContext.owner);
      for (Map<String,Either<Material,String>> map : iterable) {
        for (String key : map.keySet()) {
          if (!textures.containsKey(key) && owner.hasMaterial(key)) {
            textures.put(key, owner.getMaterial(key));
          }
        }
      }
    }
    // fetch extra textures
    if (extraTexturesKey != null) {
      Map<String,ResourceLocation> extraTextures = TextureMapManager.INSTANCE.getTextureMap(Minecraft.getInstance().getResourceManager(), extraTexturesKey);
      for (Entry<String,ResourceLocation> extra : extraTextures.entrySet()) {
        String key = extra.getKey();
        if (!textures.containsKey(key)) {
          textures.put(key, new Material(InventoryMenu.BLOCK_ATLAS, extra.getValue()));
        }
      }
    }
  }

  /** Bakes a model for the given texture */
  private static BakedModel bakeModel(IGeometryBakingContext owner, Material texture, Function<Material,TextureAtlasSprite> spriteGetter, Transformation rotation, ItemOverrides overrides) {
    TextureAtlasSprite sprite = spriteGetter.apply(texture);
    CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(owner, sprite, overrides, owner.getTransforms());
    builder.addQuads(MantleItemLayerModel.getDefaultRenderType(owner), MantleItemLayerModel.getQuadsForSprite(-1, -1, sprite, rotation, 0));
    return builder.build();
  }

  @Override
  public BakedModel bake(IGeometryBakingContext owner, ModelBaker baker, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides) {
    // setup transforms
    Transformation transform = MantleItemLayerModel.applyTransform(modelTransform, owner.getRootTransform()).getRotation();
    // build variants map
    Map<String, BakedModel> variants = new HashMap<>(textures.size());
    for (Entry<String,Material> entry : textures.entrySet()) {
      String key = entry.getKey();
      if (!key.equals("default")) {
        variants.put(key, bakeModel(owner, entry.getValue(), spriteGetter, transform, ItemOverrides.EMPTY));
      }
    }
    return bakeModel(owner, textures.get("default"), spriteGetter, transform, new Overrides(key, textures, Map.copyOf(variants)));
  }

  /** Overrides list for a tool slot item model */
  @RequiredArgsConstructor
  public static class Overrides extends ItemOverrides {
    private final ItemKey key;
    private final Map<String,Material> textures;
    private final Map<String,BakedModel> variants;

    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity, int pSeed) {
      String key = this.key.getKey(stack);
      if (!key.isEmpty()) {
        return variants.getOrDefault(key, model);
      }
      return model;
    }

    /** Gets the given texture from the model */
    @SuppressWarnings("unused")  // API usage
    public Material getTexture(String name) {
      Material texture = textures.get(name);
      return texture != null ? texture : textures.get("default");
    }
  }
}

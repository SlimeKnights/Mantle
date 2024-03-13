package slimeknights.mantle.client.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.CompositeModel;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.client.model.util.ModelTextureIteratable;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/** Model which uses a key in NBT to select which texture variant to load. */
@RequiredArgsConstructor
public class NBTKeyModel implements IUnbakedGeometry<NBTKeyModel> {
  /** Model loader instance */
  public static final IGeometryLoader<NBTKeyModel> LOADER = NBTKeyModel::deserialize;

  /** Map of statically registered extra textures, used for addon mods */
  private static final Multimap<ResourceLocation,Pair<String,ResourceLocation>> EXTRA_TEXTURES = HashMultimap.create();

  /**
   * Registers an extra variant texture for the model with the given key. Note that resource packs can override the extra texture
   * @param key          Model key, should be defined in the model JSON if supported
   * @param textureName  Name of the texture defined, corresponds to a possible value of the NBT key
   * @param texture      Texture to use, same format as in resource packs
   */
  @SuppressWarnings("unused")  // API
  public static void registerExtraTexture(ResourceLocation key, String textureName, ResourceLocation texture) {
    EXTRA_TEXTURES.put(key, Pair.of(textureName, texture));
  }

  /** Key to check in item NBT */
  private final String nbtKey;
  /** Key denoting which extra textures to fetch from the map */
  @Nullable
  private final ResourceLocation extraTexturesKey;

  /** Map of textures for the model */
  private Map<String,Material> textures = Collections.emptyMap();

  @Override
  public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    textures = new HashMap<>();
    // must have a default
    Material defaultTexture = owner.getMaterial("default");
    textures.put("default", defaultTexture);
    if (Objects.equals(defaultTexture.texture(), MissingTextureAtlasSprite.getLocation())) {
      missingTextureErrors.add(Pair.of("default", owner.getModelName()));
    }
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
      for (Pair<String,ResourceLocation> extra : EXTRA_TEXTURES.get(extraTexturesKey)) {
        String key = extra.getFirst();
        if (!textures.containsKey(key)) {
          textures.put(key, new Material(InventoryMenu.BLOCK_ATLAS, extra.getSecond()));
        }
      }
    }
    // map doubles as a useful set for the return
    return textures.values();
  }

  /** Bakes a model for the given texture */
  private static BakedModel bakeModel(IGeometryBakingContext owner, Material texture, Function<Material,TextureAtlasSprite> spriteGetter, Transformation rotation, ItemOverrides overrides) {
    TextureAtlasSprite sprite = spriteGetter.apply(texture);
    CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(owner, sprite, overrides, owner.getTransforms());
    builder.addQuads(MantleItemLayerModel.getDefaultRenderType(owner), MantleItemLayerModel.getQuadsForSprite(-1, -1, sprite, rotation, 0));
    return builder.build();
  }

  @Override
  public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
    // setup transforms
    Transformation transform = MantleItemLayerModel.applyTransform(modelTransform, owner.getRootTransform()).getRotation();
    // build variants map
    ImmutableMap.Builder<String, BakedModel> variants = ImmutableMap.builder();
    for (Entry<String,Material> entry : textures.entrySet()) {
      String key = entry.getKey();
      if (!key.equals("default")) {
        variants.put(key, bakeModel(owner, entry.getValue(), spriteGetter, transform, ItemOverrides.EMPTY));
      }
    }
    return bakeModel(owner, textures.get("default"), spriteGetter, transform, new Overrides(nbtKey, textures, variants.build()));
  }

  /** Overrides list for a tool slot item model */
  @RequiredArgsConstructor
  public static class Overrides extends ItemOverrides {
    private final String nbtKey;
    private final Map<String,Material> textures;
    private final Map<String,BakedModel> variants;

    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity, int pSeed) {
      CompoundTag nbt = stack.getTag();
      if (nbt != null && nbt.contains(nbtKey)) {
        return variants.getOrDefault(nbt.getString(nbtKey), model);
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

  /** Deserializes this model from JSON */
  public static NBTKeyModel deserialize(JsonObject json, JsonDeserializationContext context) {
    String key = GsonHelper.getAsString(json, "nbt_key");
    ResourceLocation extraTexturesKey = null;
    if (json.has("extra_textures_key")) {
      extraTexturesKey = JsonHelper.getResourceLocation(json, "extra_textures_key");
    }
    return new NBTKeyModel(key, extraTexturesKey);
  }
}

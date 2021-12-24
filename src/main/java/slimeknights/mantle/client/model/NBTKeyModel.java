package slimeknights.mantle.client.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Transformation;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;
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

@RequiredArgsConstructor
public class NBTKeyModel implements IModelGeometry<NBTKeyModel> {
  /** Model loader instance */
  public static final Loader LOADER = new Loader();

  /** Map of statically registered extra textures, used for addon mods */
  private static final Multimap<ResourceLocation,Pair<String,ResourceLocation>> EXTRA_TEXTURES = HashMultimap.create();

  /**
   * Registers an extra variant texture for the model with the given key. Note that resource packs can override the extra texture
   * @param key          Model key, should be defined in the model JSON if supported
   * @param textureName  Name of the texture defined, corresponds to a possible value of the NBT key
   * @param texture      Texture to use, same format as in resource packs
   */
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
  public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    textures = new HashMap<>();
    // must have a default
    Material defaultTexture = owner.resolveTexture("default");
    textures.put("default", defaultTexture);
    if (Objects.equals(defaultTexture.texture(), MissingTextureAtlasSprite.getLocation())) {
      missingTextureErrors.add(Pair.of("default", owner.getModelName()));
    }
    // fetch others
    UnbakedModel model = owner.getOwnerModel();
    if (model instanceof BlockModel) {
      ModelTextureIteratable iterable = new ModelTextureIteratable(null, (BlockModel) model);
      for (Map<String,Either<Material,String>> map : iterable) {
        for (String key : map.keySet()) {
          if (!textures.containsKey(key) && owner.isTexturePresent(key)) {
            textures.put(key, owner.resolveTexture(key));
          }
        }
      }
    }
    // fetch extra textures
    if (extraTexturesKey != null) {
      for (Pair<String,ResourceLocation> extra : EXTRA_TEXTURES.get(extraTexturesKey)) {
        String key = extra.getFirst();
        if (!textures.containsKey(key)) {
          textures.put(key, ModelLoaderRegistry.blockMaterial(extra.getSecond()));
        }
      }
    }
    // map doubles as a useful set for the return
    return textures.values();
  }

  /** Bakes a model for the given texture */
  private static BakedModel bakeModel(IModelConfiguration owner, Material texture, Function<Material,TextureAtlasSprite> spriteGetter, ImmutableMap<TransformType,Transformation> transformMap, ItemOverrides overrides) {
    TextureAtlasSprite sprite = spriteGetter.apply(texture);
    ImmutableList<BakedQuad> quads = ItemLayerModel.getQuadsForSprite(-1, sprite, Transformation.identity());
    return new BakedItemModel(quads, sprite, transformMap, overrides, true, owner.isSideLit());
  }

  @Override
  public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
    ImmutableMap.Builder<String, BakedModel> variants = ImmutableMap.builder();
    ImmutableMap<TransformType,Transformation> transformMap = Maps.immutableEnumMap(PerspectiveMapWrapper.getTransforms(owner.getCombinedTransform()));
    for (Entry<String,Material> entry : textures.entrySet()) {
      String key = entry.getKey();
      if (!key.equals("default")) {
        variants.put(key, bakeModel(owner, entry.getValue(), spriteGetter, transformMap, ItemOverrides.EMPTY));
      }
    }
    return bakeModel(owner, textures.get("default"), spriteGetter, transformMap, new Overrides(nbtKey, textures, variants.build()));
  }

  /** Overrides list for a tool slot item model */
  @RequiredArgsConstructor
  public static class Overrides extends ItemOverrides {
    private final String nbtKey;
    private final Map<String,Material> textures;
    private final Map<String,BakedModel> variants;

    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity) {
      CompoundTag nbt = stack.getTag();
      if (nbt != null && nbt.contains(nbtKey)) {
        return variants.getOrDefault(nbt.getString(nbtKey), model);
      }
      return model;
    }

    /** Gets the given texture from the model */
    public Material getTexture(String name) {
      Material texture = textures.get(name);
      return texture != null ? texture : textures.get("default");
    }
  }

  /** Loader logic */
  private static class Loader implements IModelLoader<NBTKeyModel> {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {}

    @Override
    public NBTKeyModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      String key = GsonHelper.getAsString(modelContents, "nbt_key");
      ResourceLocation extraTexturesKey = null;
      if (modelContents.has("extra_textures_key")) {
        extraTexturesKey = JsonHelper.getResourceLocation(modelContents, "extra_textures_key");
      }
      return new NBTKeyModel(key, extraTexturesKey);
    }
  }
}

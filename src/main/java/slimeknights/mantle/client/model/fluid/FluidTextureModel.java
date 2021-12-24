package slimeknights.mantle.client.model.fluid;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.registration.ModelFluidAttributes;
import slimeknights.mantle.registration.ModelFluidAttributes.IFluidModelProvider;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** Fluid model that allows a resource pack to control the textures of a block. Use alongside {@link ModelFluidAttributes} */
@RequiredArgsConstructor
public class FluidTextureModel implements IModelGeometry<FluidTextureModel> {
  public static Loader LOADER = new Loader();

  private final int color;

  /** Checks if a texture is missing */
  private static boolean isMissing(RenderMaterial material) {
    return MissingTextureSprite.getLocation().equals(material.texture());
  }

  /** Gets the texture, or null if missing */
  private static void getTexture(IModelConfiguration owner, String name, Collection<RenderMaterial> textures, Set<Pair<String,String>> missingTextureErrors) {
    RenderMaterial material = owner.resolveTexture(name);
    if (isMissing(material)) {
      missingTextureErrors.add(Pair.of(name, owner.getModelName()));
    }
    textures.add(material);
  }

  @Override
  public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation,IUnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    Set<RenderMaterial> textures = new HashSet<>();
    getTexture(owner, "still", textures, missingTextureErrors);
    getTexture(owner, "flowing", textures, missingTextureErrors);
    RenderMaterial overlay = owner.resolveTexture("overlay");
    if (!isMissing(overlay)) {
      textures.add(overlay);
    }
    return textures;
  }

  @Override
  public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial,TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
    RenderMaterial still = owner.resolveTexture("still");
    RenderMaterial flowing = owner.resolveTexture("flowing");
    RenderMaterial overlay = owner.resolveTexture("overlay");
    ResourceLocation overlayLocation = isMissing(overlay) ? null : overlay.texture();
    IBakedModel baked = new SimpleBakedModel.Builder(owner, overrides).particle(spriteGetter.apply(still)).build();
    return new BakedModel(baked, still.texture(), flowing.texture(), overlayLocation, color);
  }

  /** Data holder class, has no quads */
  private static class BakedModel extends BakedModelWrapper<IBakedModel> {
    @Getter
    private final ResourceLocation still;
    @Getter
    private final ResourceLocation flowing;
    @Getter
    private final ResourceLocation overlay;
    @Getter
    private final int color;
    public BakedModel(IBakedModel originalModel, ResourceLocation still, ResourceLocation flowing, @Nullable ResourceLocation overlay, int color) {
      super(originalModel);
      this.still = still;
      this.flowing = flowing;
      this.overlay = overlay;
      this.color = color;
    }
  }

  /** Model loader, also doubles as the fluid model provider */
  private static class Loader implements IModelLoader<FluidTextureModel>, IFluidModelProvider {
    private final Map<Fluid,BakedModel> modelCache = new HashMap<>();

    /** Gets a model for a fluid */
    @Nullable
    private BakedModel getFluidModel(Fluid fluid) {
      return ModelHelper.getBakedModel(fluid.defaultFluidState().createLegacyBlock(), BakedModel.class);
    }

    /** Gets a model for a fluid from the cache */
    @Nullable
    private BakedModel getCachedModel(Fluid fluid) {
      return modelCache.computeIfAbsent(fluid, this::getFluidModel);
    }

    @Override
    @Nullable
    public ResourceLocation getStillTexture(Fluid fluid) {
      BakedModel model = getCachedModel(fluid);
      return model == null ? null : model.getStill();
    }

    @Override
    @Nullable
    public ResourceLocation getFlowingTexture(Fluid fluid) {
      BakedModel model = getCachedModel(fluid);
      return model == null ? null : model.getFlowing();
    }

    @Override
    @Nullable
    public ResourceLocation getOverlayTexture(Fluid fluid) {
      BakedModel model = getCachedModel(fluid);
      return model == null ? null : model.getOverlay();
    }

    @Override
    public int getColor(Fluid fluid) {
      BakedModel model = getCachedModel(fluid);
      return model == null ? -1 : model.getColor();
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
      modelCache.clear();
    }

    @Override
    public FluidTextureModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      int color = -1;
      if (modelContents.has("color")) {
        String colorString = JSONUtils.getAsString(modelContents, "color");
        int length = colorString.length();
        // prevent some invalid strings, colors should all be 6 or 8 digits
        if (colorString.charAt(0) == '-' || (length != 6 && length != 8)) {
          throw new JsonSyntaxException("Invalid color '" + colorString + "'");
        }
        try {
          color = (int)Long.parseLong(colorString, 16);
          // for 6 length, make fully opaque
          if (length == 6) {
            color |= 0xFF000000;
          }
        } catch (NumberFormatException e) {
          throw new JsonSyntaxException("Invalid color '" + colorString + "'");
        }
      }
      return new FluidTextureModel(color);
    }
  }
}

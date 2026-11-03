/*
 * Minecraft Forge
 * Copyright (c) 2016-2021.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package slimeknights.mantle.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.client.model.SimpleModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackLinkedSet;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.util.DataComponentHelper;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Rework of {@link DynamicFluidContainerModel} with two additional features: baked tints and fluid stack sensitive models.
 * Does not handle covers as I have never seen a need for them, and it means less code duplication (plus the Neo model does the whole cover is mask thing wrong compared to 1.18).
 */
public record FluidContainerModel(FluidStack fluid, boolean flipGas, @Nullable ResourceLocation textureOverrides) implements IUnbakedGeometry<FluidContainerModel> {
  public static final ResourceLocation ID = Mantle.getResource("fluid_container");
  public static final IGeometryLoader<FluidContainerModel> LOADER = FluidContainerModel::deserialize;

  /** Clone of same named field from {@link DynamicFluidContainerModel} */
  public static final Transformation FLUID_TRANSFORM = new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1.002f), new Quaternionf());

  /** Deserializes this model from JSON */
  public static FluidContainerModel deserialize(JsonObject json, JsonDeserializationContext context) {
    FluidStack fluidStack = FluidStack.EMPTY;
    ResourceLocation textureOverrides = null;
    // parse the fluid with an optional tag
    if (json.has("fluid")) {
      JsonElement fluidElement = json.get("fluid");
      Fluid fluid;
      DataComponentPatch components = DataComponentPatch.EMPTY;
      if (fluidElement.isJsonObject()) {
        JsonObject fluidObject = fluidElement.getAsJsonObject();
        boolean fluidValid = true;
        if (GsonHelper.getAsBoolean(fluidObject, "optional")) {
          try {
            fluid = Loadables.FLUID.getIfPresent(fluidObject, "name");
          } catch (JsonSyntaxException e) {
            Mantle.logger.debug("Caught error parsing optional fluid in mantle:fluid_container model, replacing with lava: {}", e.getMessage());
            fluid = Fluids.LAVA;
            fluidValid = false;
          }
        } else {
          fluid = Loadables.FLUID.getIfPresent(fluidObject, "name");
        }
        // skip parsing components if we did not find a valid fluid
        if (fluidValid) {
          components = Loadables.DATA_COMPONENTS.getOrDefault(fluidObject, "components", DataComponentPatch.EMPTY);
        }
      } else {
        fluid = Loadables.FLUID.convert(fluidElement, "fluid");
      }
      fluidStack = DataComponentHelper.makeStack(fluid, FluidType.BUCKET_VOLUME, components);
    } else {
      textureOverrides = JsonHelper.getResourceLocation(json, "texture_overrides", null);
    }
    boolean flipGas = GsonHelper.getAsBoolean(json, "flip_gas", true);
    return new FluidContainerModel(fluidStack, flipGas, textureOverrides);
  }

  /** Gets the given sprite, or null if the texture is not present in the model */
  @Nullable
  private static TextureAtlasSprite getSprite(IGeometryBakingContext context, Function<Material,TextureAtlasSprite> spriteGetter, String key) {
    if (context.hasMaterial(key)) {
      return spriteGetter.apply(context.getMaterial(key));
    }
    return null;
  }

  /** Bakes the model with the given fluid */
  private static BakedModel bakeInternal(IGeometryBakingContext context, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, FluidStack fluid, boolean flipGas, @Nullable TextureAtlasSprite overrideSprite) {
    // get basic sprites
    IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid.getFluid());
    TextureAtlasSprite baseSprite = getSprite(context, spriteGetter, "base");
    TextureAtlasSprite fluidSprite = overrideSprite == null && !fluid.isEmpty() ? spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, clientFluid.getStillTexture(fluid))) : null;

    // determine particle
    TextureAtlasSprite particleSprite = getSprite(context, spriteGetter, "particle");
    if (particleSprite == null) particleSprite = fluidSprite;
    if (particleSprite == null) particleSprite = baseSprite;
    if (particleSprite == null) {
      Mantle.logger.error("No valid particle sprite for fluid container model, you should supply either 'base' or 'particle'");
      particleSprite = spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation()));
    }

    // if its a gas and we flipping, flip it
    if (flipGas && !fluid.isEmpty() && fluid.getFluid().getFluidType().isLighterThanAir()) {
      modelState = new SimpleModelState(modelState.getRotation().compose(new Transformation(null, new Quaternionf(0, 0, 1, 0), null, null)));
    }

    // start building the mode
    CompositeModel.Baked.Builder modelBuilder = CompositeModel.Baked.builder(context, particleSprite, overrides, context.getTransforms());
    RenderTypeGroup renderTypes = DynamicFluidContainerModel.getLayerRenderTypes(false);

    // add in fluid
    ItemLayerPixels pixels = null;
    List<BakedQuad> fluidQuads = List.of();
    RenderTypeGroup fluidRenderTypes = renderTypes;
    if (overrideSprite != null || fluidSprite != null) {
      // get light and color
      int color = clientFluid.getTintColor(fluid);
      int light = fluid.getFluid().getFluidType().getLightLevel(fluid);
      if (light > 0) {
        fluidRenderTypes = DynamicFluidContainerModel.getLayerRenderTypes(true);
      }
      // get color
      // if given, use override
      if (overrideSprite != null) {
        // want to prevent z-fighting so using the mantle quad builder
        pixels = new ItemLayerPixels();
        fluidQuads = MantleItemLayerModel.getQuadsForSprite(-1, -1, overrideSprite, modelState.getRotation(), light, pixels);
      } else {
        fluidQuads = UnbakedGeometryHelper.bakeElements(
          UnbakedGeometryHelper.createUnbakedItemMaskElements(1, spriteGetter.apply(context.getMaterial("fluid"))),
          $ -> fluidSprite,
          new SimpleModelState(modelState.getRotation().compose(FLUID_TRANSFORM), modelState.isUvLocked())
        );
        // apply light and color
        if (light > 0) {
          QuadTransformers.settingEmissivity(light).processInPlace(fluidQuads);
        }
        if (color != -1) {
          ColoredBlockModel.applyColorQuadTransformer(color).processInPlace(fluidQuads);
        }
      }
    }

    // add in the base - done second as we need the pixel information from the override sprite
    if (baseSprite != null) {
      modelBuilder.addQuads(renderTypes, MantleItemLayerModel.getQuadsForSprite(-1, -1, baseSprite, modelState.getRotation(), 0, pixels));
    }

    // want fluid quads added after base quads, but need to process base quads late for handling pixels
    if (!fluidQuads.isEmpty()) {
      modelBuilder.addQuads(fluidRenderTypes, fluidQuads);
    }
    return modelBuilder.build();
  }

  @Override
  public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
    // We need to disable GUI 3D and block lighting for this to render properly
    context = StandaloneGeometryBakingContext.builder(context).withGui3d(false).withUseBlockLight(false).build(ID);
    // only do contained fluid if we did not set the fluid in the model properties
    Map<Fluid,TextureAtlasSprite> fluidOverrides = Map.of();
    if (fluid.isEmpty()) {
      if (textureOverrides != null) {
        Map<String,ResourceLocation> textures = TextureMapManager.INSTANCE.getTextureMap(Minecraft.getInstance().getResourceManager(), textureOverrides);
        Map<Fluid,TextureAtlasSprite> builder = new HashMap<>();
        for (Map.Entry<String,ResourceLocation> entry : textures.entrySet()) {
          String key = entry.getKey();
          ResourceLocation texture = entry.getValue();
          try {
            Fluid fluid = Loadables.FLUID.parseString(key, textureOverrides.toString());
            // only fetching the sprite over a full build as the fluid might have stack sensitive properties - e.g. potion tint
            builder.put(fluid, spriteGetter.apply(new Material(InventoryMenu.BLOCK_ATLAS, texture)));
          } catch (JsonParseException e) {
            Mantle.logger.error("Failed to parse fluid container texture override {} as {} is not a fluid", textures, key, e);
          }
        }
        fluidOverrides = Map.copyOf(builder);
      }
      overrides = new ContainedFluidOverrideHandler(context, overrides, modelState, flipGas, fluidOverrides);
    }
    return bakeInternal(context, spriteGetter, modelState, overrides, fluid, flipGas, null);
  }

  /** Handles swapping the model based on the contained fluid */
  @RequiredArgsConstructor
  private static final class ContainedFluidOverrideHandler extends ItemOverrides {
    // contains all the baked models since they'll never change
    private final Map<FluidStack,BakedModel> cache = new Object2ObjectOpenCustomHashMap<>(FluidStackLinkedSet.TYPE_AND_COMPONENTS);

    /** Context for baking models dynamically */
    private final IGeometryBakingContext context;
    /** Item overrides to apply before rebaking */
    private final ItemOverrides nested;
    /** Model state for dynamic baking */
    private final ModelState modelState;
    /** If true, dynamically baked models will be flipped vertically if a gas */
    private final boolean flipGas;
    /** Map of overrides for specific fluids */
    private final Map<Fluid,TextureAtlasSprite> fluidOverrides;


    /** Gets the model directly, for creating the cached models */
    private BakedModel getUncachedModel(FluidStack fluid) {
      return bakeInternal(context, Material::sprite, modelState, ItemOverrides.EMPTY, fluid, flipGas, fluidOverrides.get(fluid.getFluid()));
    }

    /** Function to bake a model for the cache */
    private final Function<FluidStack,BakedModel> BAKER = this::getUncachedModel;

    @Nullable
    @Override
    public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
      BakedModel overriden = nested.resolve(originalModel, stack, world, entity, seed);
      if (overriden != originalModel) return overriden;
      Optional<FluidStack> optional = FluidUtil.getFluidContained(stack);
      if (optional.isPresent()) {
        FluidStack fluid = optional.get();
        return cache.computeIfAbsent(fluid, BAKER);
      }
      return originalModel;
    }
  }
}

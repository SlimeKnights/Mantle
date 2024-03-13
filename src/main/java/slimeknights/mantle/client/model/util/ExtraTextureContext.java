package slimeknights.mantle.client.model.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import java.util.Map;

/**
 * Model configuration wrapper to add in an extra set of textures
 */
public class ExtraTextureContext extends GeometryContextWrapper {
  private final Map<String,Material> textures;

  /**
   * Creates a new wrapper using the given textures
   * @param base      Base configuration
   * @param textures  Textures map, any textures in this map will take precedence over those in the base configuration
   */
  public ExtraTextureContext(IGeometryBakingContext base, Map<String,Material> textures) {
    super(base);
    this.textures = textures;
  }

  /**
   * Creates a new wrapper for a single texture
   * @param base     Base configuration
   * @param name     Texture name, if it matches texture is returned
   * @param texture  Texture path
   */
  public ExtraTextureContext(IGeometryBakingContext base, String name, ResourceLocation texture) {
    super(base);
    this.textures = ImmutableMap.of(name, new Material(InventoryMenu.BLOCK_ATLAS, texture));
  }

  @Override
  public Material getMaterial(String name) {
    Material connected = textures.get(name);
    if (connected != null) {
      return connected;
    }
    return super.getMaterial(name);
  }

  @Override
  public boolean hasMaterial(String name) {
    return textures.containsKey(name) || super.hasMaterial(name);
  }
}

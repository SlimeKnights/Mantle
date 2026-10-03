package slimeknights.mantle.client.model;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.apache.commons.lang3.math.NumberUtils;
import slimeknights.mantle.Mantle;

import java.awt.Color;
import java.util.function.ToIntFunction;

/** Helper for getting the average color of a texture */
public class TextureColorHelper {
  private TextureColorHelper() {}

  /** Cache of the color of various textures */
  private static final Object2IntMap<ResourceLocation> SPRITE_CACHE = new Object2IntOpenHashMap<>();
  /** Cache of the color of various textures */
  private static final Object2IntMap<Item> ITEM_CACHE = new Object2IntOpenHashMap<>();
  /** Cache of the color of various textures */
  private static final Object2IntMap<Block> BLOCK_CACHE = new Object2IntOpenHashMap<>();
  /** Reload listener for client utils */
  public static final ResourceManagerReloadListener RELOAD_LISTENER = manager -> {
    SPRITE_CACHE.clear();
    ITEM_CACHE.clear();
    BLOCK_CACHE.clear();
  };

  /** Gets the average color for a sprite, used internally by colorCache. Licensed under <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0</a> */
  private static int computeAverageColor(TextureAtlasSprite sprite) {
    float r = 0, g = 0, b = 0, count = 0;
    float[] hsb = new float[3];
    try {
      SpriteContents contents = sprite.contents();
      for (int x = 0; x < contents.width(); x++) {
        for (int y = 0; y < contents.height(); y++) {
          int argb = sprite.getPixelRGBA(0, x, y);
          // integer is in format of 0xAABBGGRR
          int cr = argb & 0xFF;
          int cg = argb >> 8 & 0xFF;
          int cb = argb >> 16 & 0xFF;
          int ca = argb >> 24 & 0xFF;
          if (ca > 0x7F && NumberUtils.max(cr, cg, cb) > 0x1F) {
            Color.RGBtoHSB(ca, cr, cg, hsb);
            float weight = hsb[1];
            r += cr * weight;
            g += cg * weight;
            b += cb * weight;
            count += weight;
          }
        }
      }
    } catch (Exception e) {
      // there is a random bug where models do not properly load, leading to a null frame data
      // so just catch that and treat it as another error state
      Mantle.logger.error("Caught exception reading sprite for {}", sprite.contents().name(), e);
      return -1;
    }
    if (count > 0) {
      r /= count;
      g /= count;
      b /= count;
    }
    return 0xFF000000 | (int)r << 16 | (int)g << 8 | (int)b;
  }

  /** Getter mapping a block sprite texture to a single average color */
  private static final ToIntFunction<ResourceLocation> COMPUTE_SPRITE_COLOR = key -> {
    Minecraft mc = Minecraft.getInstance();
    TextureAtlasSprite sprite = mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(key);
    //noinspection ConstantValue  eh, its better to be safe
    if (sprite == null || sprite.contents().name() == MissingTextureAtlasSprite.getLocation()) {
      return -1;
    }
    return getAverageColor(sprite);
  };

  /** Gets the color for the given texture */
  public static int getAverageColor(ResourceLocation texture) {
    return SPRITE_CACHE.computeIfAbsent(texture, COMPUTE_SPRITE_COLOR);
  }

  /** Gets the color for the given sprite. Should be from {@link InventoryMenu#BLOCK_ATLAS} */
  public static int getAverageColor(TextureAtlasSprite sprite) {
    ResourceLocation name = sprite.contents().name();
    if (SPRITE_CACHE.containsKey(name)) {
      return SPRITE_CACHE.get(name);
    }
    int color = computeAverageColor(sprite);
    SPRITE_CACHE.put(name, color);
    return color;
  }


  /* Particle textures */

  /** Computes the color for an item based on the particle icon */
  private static final ToIntFunction<Item> COMPUTE_ITEM_COLOR = item -> {
    Minecraft mc = Minecraft.getInstance();
    BakedModel model = mc.getItemRenderer().getModel(new ItemStack(item), null, null, 0);
    if (model == mc.getModelManager().getMissingModel()) {
      return -1;
    }
    return getAverageColor(model.getParticleIcon(ModelData.EMPTY));
  };

  /** Gets the average color of an item's default particle icon */
  public static int getItemColor(ItemLike item) {
    return ITEM_CACHE.computeIfAbsent(item.asItem(), COMPUTE_ITEM_COLOR);
  }

  /** Computes the color for an item based on the particle icon */
  private static final ToIntFunction<Block> COMPUTE_BLOCK_COLOR = block -> {
    Minecraft mc = Minecraft.getInstance();
    BakedModel model = mc.getBlockRenderer().getBlockModel(block.defaultBlockState());
    if (model == mc.getModelManager().getMissingModel()) {
      return -1;
    }
    return getAverageColor(model.getParticleIcon(ModelData.EMPTY));
  };

  /** Gets the average color of an blocks default particle icon */
  public static int getBlockColor(Block block) {
    return BLOCK_CACHE.computeIfAbsent(block, COMPUTE_BLOCK_COLOR);
  }
}

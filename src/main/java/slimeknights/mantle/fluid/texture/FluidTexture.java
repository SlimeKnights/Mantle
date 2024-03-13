package slimeknights.mantle.fluid.texture;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.util.IdExtender.LocationExtender;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Objects;

/** Record representing a fluid texture */
public record FluidTexture(ResourceLocation still, ResourceLocation flowing, @Nullable ResourceLocation overlay, @Nullable ResourceLocation camera, int color) {

  /** Serializes this to JSON */
  public JsonObject serialize() {
    JsonObject json = new JsonObject();
    json.addProperty("still", still.toString());
    json.addProperty("flowing", flowing.toString());
    if (overlay != null) {
      json.addProperty("overlay", overlay.toString());
    }
    // during datagen, we just write the texture directly, we will include the needed prefix/suffix on read
    if (camera != null) {
      json.addProperty("camera", camera.toString());
    }
    json.addProperty("color", String.format("%08X", color));
    return json;
  }

  /** Deserializes this from JSON */
  public static FluidTexture deserialize(JsonObject json) {
    ResourceLocation still = JsonHelper.getResourceLocation(json, "still");
    ResourceLocation flowing = JsonHelper.getResourceLocation(json, "flowing");
    //noinspection ConstantConditions
    ResourceLocation overlay = JsonHelper.getResourceLocation(json, "overlay", null);
    ResourceLocation camera = null;
    if (json.has("camera")) {
      camera = LocationExtender.INSTANCE.wrap(JsonHelper.getResourceLocation(json, "camera"), "textures/", ".png");
    }
    int color = JsonHelper.parseColor(GsonHelper.getAsString(json, "color"));
    return new FluidTexture(still, flowing, overlay, camera, color);
  }

  /** Builder for this object */
  @SuppressWarnings("unused") // API
  @Setter
  @Accessors(fluent = true)
  @RequiredArgsConstructor
  public static class Builder {
    private final FluidType fluid;
    private ResourceLocation still;
    private ResourceLocation flowing;
    @Nullable
    private ResourceLocation overlay = null;
    @Nullable
    private ResourceLocation camera = null;
    private int color = -1;

    /**
     * Adds textures using the fluid registry ID
     * @param prefix     Prefix for where to place textures
     * @param suffix     Suffix for placing textures, included before "still" or "flowing". Typically will want "/" or "_".
     * @param overlay    If true, include an overlay texture
     * @param camera     If true, include a camera texture
     * @return  Builder instance
     */
    public Builder wrapId(String prefix, String suffix, boolean overlay, boolean camera) {
      return textures(LocationExtender.INSTANCE.wrap(Objects.requireNonNull(ForgeRegistries.FLUID_TYPES.get().getKey(fluid)), prefix, suffix), overlay, camera);
    }

    /**
     * Sets all textures by suffixing the given path
     * @param path     Base path, make sure to include the trailing "_" or "/"
     * @param overlay  If true, include an overlay texture
     * @param camera   If true, include a camera texture
     * @return  Builder instance
     */
    public Builder textures(ResourceLocation path, boolean overlay, boolean camera) {
      still(LocationExtender.INSTANCE.suffix(path, "still"));
      flowing(LocationExtender.INSTANCE.suffix(path, "flowing"));
      if (overlay) {
        overlay(LocationExtender.INSTANCE.suffix(path, "overlay"));
      }
      if (camera) {
        camera(LocationExtender.INSTANCE.suffix(path, "camera"));
      }
      return this;
    }

    /** Builds the fluid texture instance */
    public FluidTexture build() {
      if (still == null || flowing == null) {
        throw new IllegalStateException("Must set both stll and flowing");
      }
      return new FluidTexture(still, flowing, overlay, camera, color);
    }
  }
}

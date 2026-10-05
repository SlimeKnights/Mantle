package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.Mantle;

/** Loader for {@link slimeknights.mantle.client.model.NBTKeyModel} */
@Setter
@Accessors(fluent = true)
public class NBTKeyModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  private String key = null;
  private ResourceLocation extraTexturesKey = null;
  public NBTKeyModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
    super(Mantle.getResource("nbt_key"), parent, existingFileHelper);
  }

  /** @deprecated use {@link #NBTKeyModelBuilder(T, ExistingFileHelper)} */
  @SuppressWarnings("unused")
  @Deprecated(forRemoval = true)
  public NBTKeyModelBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper) {
    this(parent, existingFileHelper);
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    if (key == null) {
      throw new IllegalStateException("Must set key to use NBTKeyModel");
    }
    json = super.toJson(json);
    json.addProperty("nbt_key", key);
    if (extraTexturesKey != null) {
      json.addProperty("extra_textures_key", extraTexturesKey.toString());
    }
    return json;
  }
}

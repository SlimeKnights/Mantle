package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.client.model.ItemKey;
import slimeknights.mantle.client.model.ItemKeyModel;

/** Loader for {@link ItemKeyModel} */
@SuppressWarnings("unused")  // API
@Setter
@Accessors(fluent = true)
public class ItemKeyModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  private ItemKey key = null;
  private ResourceLocation extraTexturesKey = null;
  public ItemKeyModelBuilder(T parent, ExistingFileHelper existingFileHelper, boolean allowInlineElements) {
    super(ItemKeyModel.ID, parent, existingFileHelper, allowInlineElements);
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    if (key == null) {
      throw new IllegalStateException("Must set key to use NBTKeyModel");
    }
    json = super.toJson(json);
    json.add("key", ItemKey.LOADER.serialize(key));
    if (extraTexturesKey != null) {
      json.addProperty("extra_textures_key", extraTexturesKey.toString());
    }
    return json;
  }
}

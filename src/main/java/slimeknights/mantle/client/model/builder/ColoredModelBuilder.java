package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.util.ColoredBlockModel.ColorData;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link slimeknights.mantle.client.model.util.ColoredBlockModel}, used as a base for other model builders.
 * @param <T>  Builder type
 */
public class ColoredModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  private final List<ColorData> colors = new ArrayList<>();

  public ColoredModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
    this(Mantle.getResource("colored_block"), parent, existingFileHelper);
  }

  protected ColoredModelBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper) {
    super(loaderId, parent, existingFileHelper, false);
  }

  /** Adds a full color data for the next element */
  public ColoredModelBuilder<T> colorData(ColorData data) {
    colors.add(data);
    return this;
  }

  /** Sets the color for the next element */
  public ColoredModelBuilder<T> color(int color) {
    return colorData(new ColorData(color, -1, null));
  }

  /** Sets the luminosity for the next element */
  public ColoredModelBuilder<T> luminosity(int luminosity) {
    return colorData(new ColorData(-1, luminosity, null));
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    json = super.toJson(json);
    if (!colors.isEmpty()) {
      json.add("colors", ColorData.LIST_LOADABLE.serialize(colors));
    }
    return json;
  }
}

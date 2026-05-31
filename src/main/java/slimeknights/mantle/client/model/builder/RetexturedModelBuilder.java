package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.Mantle;

public class RetexturedModelBuilder<T extends ModelBuilder<T>> extends ColoredModelBuilder<T> {
  private final JsonArray retextured = new JsonArray();
  public RetexturedModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
    super(Mantle.getResource("retextured"), parent, existingFileHelper);
  }

  /** Marks the given texture as retextured. Uses the texture name, not path. */
  public RetexturedModelBuilder<T> retexture(String name) {
    this.retextured.add(name);
    return this;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    json = super.toJson(json);
    json.add("retextured", retextured);
    return json;
  }
}

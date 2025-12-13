package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.Mantle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Builder for {@link slimeknights.mantle.client.model.FallbackModelLoader} */
public class FallbackModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  private final List<DomainModel<T>> models = new ArrayList<>();
  public FallbackModelBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper) {
    super(Mantle.getResource("fallback"), parent, existingFileHelper, false);
  }

  /** Adds a fallback model with a domain restriction */
  public FallbackModelBuilder<T> fallback(T builder, @Nullable String modId) {
    this.models.add(new DomainModel<>(builder, modId));
    return this;
  }

  /** Adds a fallback model using the loader ID as the domain restriction */
  public FallbackModelBuilder<T> fallback(T builder) {
    return fallback(builder, null);
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    json = super.toJson(json);
    if (this.models.size() < 2) {
      throw new IllegalStateException("Must have at least two models to use the fallback loader");
    }
    JsonArray fallbacks = new JsonArray();
    for (DomainModel<T> builder : models) {
      fallbacks.add(builder.toJson());
    }
    json.add("models", fallbacks);
    return json;
  }

  /** Builder with an optional domain restriction */
  private record DomainModel<T extends ModelBuilder<T>>(T model, @Nullable String domain) {
    /** Converts this to JSON */
    public JsonObject toJson() {
      JsonObject json = model.toJson();
      if (domain != null) {
        json.addProperty("fallback_mod_id", domain);
      }
      return json;
    }
  }
}

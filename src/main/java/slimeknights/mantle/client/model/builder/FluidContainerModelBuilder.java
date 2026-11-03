package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeknights.mantle.client.model.FluidContainerModel;
import slimeknights.mantle.data.loadable.Loadables;

/** Builder for {@link slimeknights.mantle.client.model.FluidContainerModel} */
@SuppressWarnings("unused")  // API
public class FluidContainerModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
  private Fluid fluid = Fluids.EMPTY;
  private DataComponentPatch components;
  private boolean flipGas = true;
  private boolean optional = false;
  private ResourceLocation textureOverride = null;

  public FluidContainerModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
    super(FluidContainerModel.ID, parent, existingFileHelper, false);
  }

  /** Sets the fluid */
  public FluidContainerModelBuilder<T> fluid(Fluid fluid) {
    this.fluid = fluid;
    return this;
  }

  /** Sets the fluid components */
  public FluidContainerModelBuilder<T> components(DataComponentPatch components) {
    this.components = components;
    return this;
  }

  /** Sets whether the model flips gas fluids upsidedown */
  public FluidContainerModelBuilder<T> flipGas(boolean flipGas) {
    this.flipGas = flipGas;
    return this;
  }

  /** Marks this fluid as optional, preventing errors if the fluid is not registered. */
  public FluidContainerModelBuilder<T> fluidOptional() {
    this.optional = true;
    return this;
  }

  /**
   * Sets the path to the texture overrides model, for adding additional fluid texture overrides. Only used if {@link #fluid(Fluid)} is empty.
   * See {@link slimeknights.mantle.client.model.TextureMapManager} for more information.
   */
  public FluidContainerModelBuilder<T> textureOverride(ResourceLocation textureOverride) {
    this.textureOverride = textureOverride;
    return this;
  }

  @Override
  public JsonObject toJson(JsonObject json) {
    if (this.fluid == Fluids.EMPTY) {
      throw new IllegalStateException("Must set fluid");
    }
    json = super.toJson(json);
    if (fluid != Fluids.EMPTY) {
      JsonElement fluidElement = Loadables.FLUID.serialize(this.fluid);
      if (components.isEmpty() && !optional) {
        json.add("fluid", fluidElement);
      } else {
        JsonObject fluid = new JsonObject();
        fluid.add("name", fluidElement);
        fluid.add("components", Loadables.DATA_COMPONENTS.serialize(components));
        if (optional) {
          fluid.addProperty("optional", true);
        }
        json.add("fluid", fluid);
      }
    } else if (textureOverride != null) {
      json.addProperty("texture_override", textureOverride.toString());
    }
    json.addProperty("flip_gas", flipGas);
    return json;
  }
}

package slimeknights.mantle.client.model.builder;

import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponentPatch;
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

  @Override
  public JsonObject toJson(JsonObject json) {
    if (this.fluid == Fluids.EMPTY) {
      throw new IllegalStateException("Must set fluid");
    }
    json = super.toJson(json);
    if (components.isEmpty()) {
      json.add("fluid", Loadables.FLUID.serialize(this.fluid));
    } else {
      JsonObject fluid = new JsonObject();
      fluid.add("name", Loadables.FLUID.serialize(this.fluid));
      fluid.add("components", Loadables.DATA_COMPONENTS.serialize(components));
      json.add("fluid", fluid);
    }
    json.addProperty("flip_gas", flipGas);
    return json;
  }
}

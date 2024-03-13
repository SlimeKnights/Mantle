package slimeknights.mantle.fluid;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;

import java.util.function.Consumer;

/**
 * Fluid type whose color and textures are determined by the model.
 * Just implements {@link ClientTextureFluidType} in initializeClient as the Forge API is dumb and does not let me do that in a client place.
 */
public class TextureFluidType extends FluidType {
  public TextureFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this));
  }
}

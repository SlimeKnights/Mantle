package slimeknights.mantle.block;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import slimeknights.mantle.common.IGeneratedJson;

public class ExampleGeneratedJsonBlock extends Block implements IGeneratedJson {

  public ExampleGeneratedJsonBlock(Properties p_i48440_1_) {
    super(p_i48440_1_);
  }

  @Override
  public String getParentToUse() {
    return "block/cube_all";
  }

  @Override
  public JsonObject getTexturesToUse() {
    JsonObject textures = new JsonObject();

    textures.addProperty("all", this.getRegistryName().getNamespace() + ":block/" + this.getRegistryName().getPath());

    return textures;
  }

  @Override
  public JsonObject getVariants() {
    JsonObject variants = new JsonObject();
    JsonObject variant = new JsonObject();

    variant.addProperty("model", this.getRegistryName().getNamespace() + ":block/" + this.getRegistryName().getPath());
    variants.add("", variant);

    return variants;
  }

}

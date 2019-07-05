package slimeknights.mantle.item;

import com.google.gson.JsonObject;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import slimeknights.mantle.common.IGeneratedJson;

public class EdibleItem extends Item implements IGeneratedJson {

  public EdibleItem(Food foodIn, ItemGroup itemGroup) {
    super(new Properties().food(foodIn).group(itemGroup));
  }

  @Override
  public String getParentToUse() {
    return "item/generated";
  }

  @Override
  public JsonObject getTexturesToUse() {
    JsonObject textures = new JsonObject();

    textures.addProperty("layer0", this.getRegistryName().getNamespace() + ":item/" + this.getRegistryName().getPath() + " CHANGEME");

    return textures;
  }
}

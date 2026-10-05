package slimeknights.mantle.recipe.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.util.typed.TypedMap;

/**
 * Extension of {@link ItemOutput} for datagen of recipes for compat. Should never be used in an actual recipe
 */
@RequiredArgsConstructor(staticName = "fromName")
public class ItemNameOutput extends ItemOutput {
  private final ResourceLocation name;
  @Getter
  private final int count;
  private final DataComponentPatch components;

  /**
   * Creates an output for the given item with no NBT
   * @param name   Item name
   * @param count  Count
   * @return  Output
   */
  public static ItemNameOutput fromName(ResourceLocation name, int count) {
    return fromName(name, count, DataComponentPatch.EMPTY);
  }

  /**
   * Creates an output for the given item with a count of 1
   * @param name  Item name
   * @return  Output
   */
  public static ItemNameOutput fromName(ResourceLocation name) {
    return fromName(name, 1);
  }

  @Override
  public ItemStack get() {
    throw new UnsupportedOperationException("Cannot get the item stack from a item name output");
  }

  @Override
  public JsonElement serialize(boolean writeCount, TypedMap context) {
    String itemName = name.toString();
    if (components.isEmpty() && (count <= 1 || !writeCount)) {
      return new JsonPrimitive(itemName);
    } else {
      JsonObject jsonResult = new JsonObject();
      jsonResult.addProperty("item", itemName);
      if (writeCount) {
        jsonResult.addProperty("count", count);
      }
      if (!components.isEmpty()) {
        jsonResult.add("nbt", Loadables.DATA_COMPONENTS.serialize(components, context));
      }
      return jsonResult;
    }
  }
}

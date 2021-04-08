package slimeknights.mantle.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import static slimeknights.mantle.client.book.BookLoader.GSON;

public class CraftingHelper {

  public static ItemStack getItemStack(JsonObject json, boolean readNBT) {
    String itemName = JsonHelper.getElement(json, "item").getAsString();

    Item item = Registry.ITEM.get(new Identifier(itemName));

    if (readNBT && json.has("nbt")) {
      try {
        JsonElement element = json.get("nbt");
        CompoundTag nbt;
        if(element.isJsonObject())
          nbt = StringNbtReader.parse(GSON.toJson(element));
        else
          nbt = StringNbtReader.parse(JsonHelper.getElement(element.getAsJsonObject(), "nbt").getAsString());

        CompoundTag tmp = new CompoundTag();
        if (nbt.contains("ForgeCaps"))
        {
          tmp.put("ForgeCaps", nbt.get("ForgeCaps"));
          nbt.remove("ForgeCaps");
        }

        tmp.put("tag", nbt);
        tmp.putString("id", itemName);
        tmp.putInt("Count", JsonHelper.getElement(json, "count").getAsInt());

        return ItemStack.fromTag(tmp);
      }
      catch (CommandSyntaxException e) {
        throw new JsonSyntaxException("Invalid NBT Entry: " + e.toString());
      }
    }

    return new ItemStack(item, JsonHelper.getElement(json, "count").getAsInt());
  }
}

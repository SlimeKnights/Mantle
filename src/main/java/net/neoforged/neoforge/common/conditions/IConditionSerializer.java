package slimeknights.mantle.compat.neoforged.neoforge.common.conditions;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

public interface IConditionSerializer<T extends ICondition> {
  ResourceLocation getID();

  void write(JsonObject json, T value);

  T read(JsonObject json);
}

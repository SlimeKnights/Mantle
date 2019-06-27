package slimeknights.mantle.common;

import com.google.gson.JsonObject;

public interface IGeneratedJson {

  String getParentToUse();

  JsonObject getTexturesToUse();

  default JsonObject getVariants() {
    return null;
  }
}
